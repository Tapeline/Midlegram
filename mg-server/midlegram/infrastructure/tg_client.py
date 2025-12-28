import tempfile
import uuid
from asyncio import Queue
from contextlib import suppress
from dataclasses import dataclass, field, replace
import asyncio
from datetime import datetime
import time

from structlog import BoundLogger, getLogger
from pathlib import Path
from typing import Any

from telegram.client import Telegram
from telegram.utils import AsyncResult

from midlegram.application.client import AuthCodeVerdict, MessengerClient
from midlegram.application.exceptions import (
    InvalidAuthCode,
    TelegramSessionExpired, UnknownClientError,
    UnknownMediaType, Wrong2FAPassword,
)
from midlegram.application.pagination import Pagination
from midlegram.config import Config
from midlegram.domain.entities import (
    Chat,
    ChatFolder,
    ChatFolderId, ChatId,
    Message,
    MessageMedia, MessageId, MessageType, User, UserId,
)
from midlegram.infrastructure.client_store import ClientFactory

logger = getLogger(__name__)
_MAX_CHATS: int = 10000


@dataclass(frozen=True, slots=True)
class DefaultTgClientFactory(ClientFactory):
    config: Config

    def new_client(self, session_path: Path) -> MessengerClient:
        return TelegramClient(
            tg=Telegram(
                api_id=self.config.tg_app.api_id,
                api_hash=self.config.tg_app.api_hash,
                database_encryption_key="default_key",
                files_directory=session_path,
                phone="no phone initially",
                use_test_dc=False,
            ),
            config=self.config,
        )


async def wait_tg(result: AsyncResult) -> AsyncResult:
    while not result._ready.is_set():
        await asyncio.sleep(0.01)
    return result


def ensure_no_error(result: AsyncResult) -> AsyncResult:
    if result.error:
        if result.error_info["code"] == 401:
            raise TelegramSessionExpired
        logger.error("Unknown client error: %s", result.error_info)
        raise UnknownClientError(result.error_info)
    return result


@dataclass(slots=True)
class TelegramClient(MessengerClient):
    config: Config
    tg: Telegram
    _listeners: list[Queue[Message]] = field(default_factory=list)
    _folders: list[ChatFolder] = field(default_factory=list)
    _folder_chat_ids: dict[ChatFolderId, list[ChatId]] = field(
        default_factory=dict
    )
    _chats_in_folders: dict[ChatFolderId, list[Chat]] = field(
        default_factory=dict
    )
    _chats: dict[ChatId, Chat] = field(default_factory=dict)
    _g_msg_queue: Queue[Message] = field(default_factory=Queue)
    _loop: asyncio.AbstractEventLoop = None
    _foldersUpdated: asyncio.Future = field(default_factory=asyncio.Future)

    def init(self) -> None:
        self._loop = asyncio.get_event_loop()
        self.tg._set_initial_params()
        self.tg.phone = None
        self.tg.add_update_handler("updateChatFolders", self._update_folders)
        self.tg.add_update_handler('updateNewMessage', self._put_new_msg)
        self.tg.add_update_handler(
            "updateChatPosition", self._handle_chat_position
        )
        self.tg.add_update_handler(
            "updateChatLastMessage", self._handle_chat_last_message
        )
        self.tg.add_update_handler(
            "updateChatReadInbox", self._handle_read_inbox
        )
        self.tg.call_method('getOption', {'name': 'version'}).wait()

    def _put_new_msg(self, update: dict[str, Any]) -> None:
        # logger.debug("Got a message", msg=update["message"])
        for q in self._listeners:
            q.put_nowait(_parse_message(update["message"]))
        self._loop.call_soon_threadsafe(
            self._handle_msg_in_loop,
            _parse_message(update["message"])
        )

    def _handle_msg_in_loop(self, message: Message) -> None:
        self._g_msg_queue.put_nowait(message)
        while self._g_msg_queue.qsize() > self.config.max_msg_queue_size:
            with suppress(asyncio.QueueEmpty):
                self._g_msg_queue.get_nowait()

    def _update_folders(self, update: dict[str, Any]) -> None:
        self._folders = [
            ChatFolder(
                id=folder["id"],
                title=folder.get("title", "Folder")
            ) for folder in update["chat_folders"]
        ]

    def _handle_chat_position(self, update: dict[str, Any]) -> None:
        chat_id = update['chat_id']
        position = update['position']
        order = position['order']  # 0 means removed, >0 means the sort order

        folder_id = None
        if position['list']['@type'] == 'chatListMain':
            folder_id = ChatFolderId(0)
        elif position['list']['@type'] == 'chatListFolder':
            folder_id = ChatFolderId(position['list']['chat_folder_id'])

        # If it's an archive list, ignore
        if folder_id is None:
            return

        if folder_id not in self._chats_in_folders:
            self._chats_in_folders[folder_id] = []
        if chat_id not in self._chats:
            self._loop.call_soon_threadsafe(
                self._load_chat_background, chat_id, folder_id, order
            )
            return

        chat = self._chats[chat_id]
        current_list = self._chats_in_folders[folder_id]

        self._chats_in_folders[folder_id] = [
            c for c in current_list if c.id != chat_id
        ]
        if order == 0:
            logger.debug("Chat removed", chat=chat_id, folder=folder_id)
        else:
            self._chats_in_folders[folder_id].insert(0, chat)
            logger.debug("Chat moved to top", chat=chat_id, folder=folder_id)

    def _handle_chat_last_message(self, update: dict[str, Any]) -> None:
        chat_id = update['chat_id']
        if chat_id not in self._chats:
            return
        last_message = update.get('last_message')
        if not last_message:
            return
        msg = _parse_message(last_message)
        updated_chat = replace(self._chats[chat_id], last_msg=msg.text)
        self._update_local_chat_instance(updated_chat)

    def _handle_read_inbox(self, update: dict[str, Any]) -> None:
        chat_id = update['chat_id']
        unread_count = update['unread_count']
        if chat_id in self._chats:
            updated_chat = replace(
                self._chats[chat_id], unread_count=unread_count
            )
            self._update_local_chat_instance(updated_chat)

    def _update_local_chat_instance(self, new_chat: Chat) -> None:
        self._chats[new_chat.id] = new_chat
        for folder_id, chat_list in self._chats_in_folders.items():
            for i, chat in enumerate(chat_list):
                if chat.id == new_chat.id:
                    chat_list[i] = new_chat
                    break

    async def _load_chat_background(
        self,
        chat_id: ChatId,
        folder_id: ChatFolderId,
        order: int
    ) -> None:
        if chat_id not in self._chats:
            try:
                self._chats[chat_id] = await self._load_chat(chat_id)
            except Exception as e:
                logger.error(
                    "Failed to load background chat", chat_id=chat_id, exc=e
                )
                return
        if order > 0 and folder_id in self._chats_in_folders:
            exists = any(
                c.id == chat_id for c in self._chats_in_folders[folder_id]
            )
            if not exists:
                self._chats_in_folders[folder_id].insert(
                    0, self._chats[chat_id]
                )

    async def connect_client(self) -> None:
        self._chats.clear()
        await asyncio.sleep(2)
        ensure_no_error(
            await wait_tg(
                self.tg.call_method(
                    'loadChats', {
                        'chat_list': {'@type': 'chatListMain'},
                        'limit': _MAX_CHATS,
                    }
                )
            )
        )
        all_chats = ensure_no_error(
            await wait_tg(self.tg.get_chats(limit=_MAX_CHATS))
        ).update["chat_ids"]
        self._chats_in_folders[ChatFolderId(0)] = list(
            await asyncio.gather(*map(self._load_chat, all_chats))
        )
        self._folder_chat_ids[ChatFolderId(0)] = [
            chat.id for chat in self._chats_in_folders[ChatFolderId(0)]
        ]
        for chats in self._chats_in_folders.values():
            for chat in chats:
                self._chats[chat.id] = chat
        for folder in self._folders:
            chat_ids = ensure_no_error(
                await wait_tg(
                    self.tg.call_method(
                        'getChats', {
                            'chat_list': {
                                '@type': 'chatListFolder',
                                'chat_folder_id': folder.id
                            },
                            'limit': _MAX_CHATS,
                        }
                    )
                )
            ).update["chat_ids"]
            self._folder_chat_ids[folder.id] = chat_ids
            self._chats_in_folders[folder.id] = []
            for chat_id in chat_ids:
                if chat_id not in self._chats:
                    logger.info(
                        "Chat was not loaded during the first round. "
                        "Is it archived?",
                        id=chat_id, folder=folder
                    )
                    self._chats[chat_id] = await self._load_chat(chat_id)
                self._chats_in_folders[folder.id].append(self._chats[chat_id])

    async def _load_chat(self, chat_id: ChatId) -> Chat:
        logger.info("Retrieving chat", chat_id=chat_id)
        result = ensure_no_error(
            await wait_tg(
                self.tg.call_method('getChat', {'chat_id': chat_id})
            )
        )
        return _parse_chat(result.update)

    async def request_phone_auth(self, phone: str) -> None:
        ensure_no_error(
            await wait_tg(
                self.tg.call_method(
                    "setAuthenticationPhoneNumber", {
                        'phone_number': phone
                    }
                )
            )
        )
        logger.info("Phone auth requested", phone=phone)

    async def is_auth_code_valid(self, code: str) -> AuthCodeVerdict:
        result = await wait_tg(
            self.tg.call_method(
                'checkAuthenticationCode', {
                    'code': code
                }
            )
        )
        if result.error:
            return AuthCodeVerdict.INVALID
        logger.info("Phone auth code ok")
        # Check if we need password (2FA)
        auth_state = ensure_no_error(
            await wait_tg(
                self.tg.call_method('getAuthorizationState')
            )
        )
        if auth_state.update['@type'] == 'authorizationStateWaitPassword':
            return AuthCodeVerdict.WAITING_2FA
        elif auth_state.update['@type'] == 'authorizationStateReady':
            return AuthCodeVerdict.OK
        else:
            logger.error(
                "Unknown auth state type: %s", auth_state.update['@type']
            )
            raise UnknownClientError

    async def auth_with_2fa(self, password: str) -> None:
        result = await wait_tg(
            self.tg.call_method(
                'checkAuthenticationPassword', {
                    'password': password
                }
            )
        )
        if result.error:
            raise Wrong2FAPassword
        logger.info("2FA login ok")

    async def get_chats_ids(
        self,
        folder_id: ChatFolderId,
        pagination: Pagination
    ) -> list[ChatId]:
        return self._folder_chat_ids.get(folder_id, [])[
               pagination.offset:pagination.offset + pagination.limit
               ]

    async def get_chat_folders(self) -> list[ChatFolder]:
        return self._folders

    async def get_chat(self, chat_id: ChatId) -> Chat:
        if chat_id not in self._chats:
            self._chats[chat_id] = await self._load_chat(chat_id)
        return self._chats[chat_id]

    async def wait_for_messages(self, timeout_s: int) -> list[Message]:
        try:
            data = [await asyncio.wait_for(
                self._g_msg_queue.get(), timeout=timeout_s
            )]
            logger.info("Got something", length=len(data))
            while not self._g_msg_queue.empty():
                data.append(self._g_msg_queue.get_nowait())
            return data  # type: ignore
        except asyncio.TimeoutError:
            logger.info("Nothing new")
            return []

    async def get_messages(
        self,
        chat_id: ChatId,
        from_msg_id: MessageId,
        limit: int
    ) -> list[Message]:
        logger.info(
            "Retrieving messages for chat",
            chat_id=chat_id,
            from_=from_msg_id,
            lim=limit
        )
        ensure_no_error(
            await wait_tg(
                self.tg.call_method('openChat', {'chat_id': chat_id})
            )
        )
        result = ensure_no_error(
            await wait_tg(
                self.tg.call_method(
                    'getChatHistory', {
                        'chat_id': chat_id,
                        'from_message_id': from_msg_id,
                        'offset': 0,
                        'limit': limit,
                        'only_local': False
                    }
                )
            )
        )
        return [_parse_message(msg) for msg in result.update["messages"]]

    async def mark_read(
        self,
        chat_id: ChatId,
        messages: list[MessageId]
    ) -> None:
        logger.info(
            "Marking read for chat",
            chat_id=chat_id, length=len(messages)
        )
        ensure_no_error(
            await wait_tg(
                self.tg.call_method(
                    'viewMessages', {
                        'chat_id': chat_id,
                        'message_ids': messages,
                        'force_read': True
                    }
                )
            )
        )

    async def send_text(
        self,
        chat_id: ChatId,
        reply_to: MessageId | None,
        text: str
    ) -> None:
        logger.info("Sending text", chat_id=chat_id)
        opts = {}
        if reply_to:
            opts["reply_to"] = {"message_id": int(reply_to)}
        ensure_no_error(
            await wait_tg(
                self.tg.call_method(
                    'sendMessage', {
                        'chat_id': chat_id,
                        'input_message_content': {
                            '@type': 'inputMessageText',
                            'text': {'text': text}
                        },
                        **opts,
                    }
                )
            )
        )

    async def send_file(
        self,
        chat_id: ChatId,
        reply_to: MessageId | None,
        media_type: str,
        contents: bytes,
    ) -> None:
        logger.info("Sending media", media_type=media_type)
        opts = {}
        tmp_path = Path(self.config.storage.media_path, str(uuid.uuid4()))
        if reply_to:
            opts["reply_to"] = {"message_id": int(reply_to)}
        if type == "photo":
            content = {
                '@type': 'inputMessagePhoto',
                'photo': {'@type': 'inputFileLocal', 'path': str(tmp_path)}
            }
        elif type == "voice_note":
            content = {
                '@type': 'inputMessageVoiceNote',
                'voice_note': {'@type': 'inputFileLocal', 'path': str(tmp_path)}
            }
        elif type == "video":
            content = {
                '@type': 'inputMessageVideo',
                'video': {'@type': 'inputFileLocal', 'path': str(tmp_path)}
            }
        elif type == "video_note":
            content = {
                '@type': 'inputMessageVideoNote',
                'video_note': {'@type': 'inputFileLocal', 'path': str(tmp_path)}
            }
        else:
            raise UnknownMediaType
        try:
            tmp_path.write_bytes(contents)
            ensure_no_error(
                await wait_tg(
                    self.tg.call_method(
                        'sendMessage', {
                            'chat_id': chat_id,
                            'input_message_content': content,
                            **opts,
                        }
                    )
                )
            )
        finally:
            tmp_path.unlink()

    def logout_and_stop(self) -> None:
        logger.info("Logging out")
        self.tg.call_method('logOut', {})
        self.stop()

    def stop(self) -> None:
        self.tg.stop()

    async def get_user(self, user_id: UserId) -> User:
        user = ensure_no_error(await wait_tg(self.tg.get_user(user_id)))
        handle = "?"
        name = f"User {user_id}"
        if user.update:
            usernames = user.update.get('usernames', {})
            handle = usernames.get('active_usernames', ['?'])[0]
            first = user.update.get('first_name', '')
            last = user.update.get('last_name', '')
            name = f"{first} {last}".strip()
        return User(
            id=user_id,
            name=name,
            handle=handle,
        )

    async def get_file_content(
        self, file_id: int, timeout_s: int = 300
    ) -> bytes:
        logger.info("Requesting file download", file_id=file_id)
        result = ensure_no_error(
            await wait_tg(
                self.tg.call_method(
                    'downloadFile',
                    {
                        'file_id': file_id,
                        'priority': 1,
                        'offset': 0,
                        'limit': 0,
                        'synchronous': False
                    }
                )
            )
        )
        file_info = result.update
        if file_info.get('local', {}).get('is_downloading_completed'):
            return self._read_file_bytes(file_info)
        start_time = asyncio.get_running_loop().time()
        while (asyncio.get_running_loop().time() - start_time) < timeout_s:
            await asyncio.sleep(0.5)
            result = ensure_no_error(
                await wait_tg(
                    self.tg.call_method('getFile', {'file_id': file_id})
                )
            )
            file_info = result.update
            if file_info.get('local', {}).get('is_downloading_completed'):
                return self._read_file_bytes(file_info)
        raise TimeoutError(
            f"File {file_id} did not download within {timeout_s} seconds"
        )

    def _read_file_bytes(self, file_info: dict[str, Any]) -> bytes:
        path_str = file_info['local']['path']
        if not path_str:
            raise UnknownClientError(
                {"type": "File path is empty", "info": file_info}
            )
        return Path(path_str).read_bytes()

    async def search_chats(self, query: str, limit: int) -> list[Chat]:
        chats = ensure_no_error(
            await wait_tg(
                self.tg.call_method(
                    "searchChatsOnServer",
                    {"query": query, "limit": limit}
                )
            )
        )
        return list(
            await asyncio.gather(
                *map(
                    self.get_chat, chats.update["chat_ids"]
                )
            )
        )


def _parse_message(msg: dict[str, Any]) -> Message:
    content = msg["content"]
    msg_type = {
        "messageText": MessageType.TEXT,
        "messagePhoto": MessageType.PHOTO,
        "messageVideo": MessageType.VIDEO,
        "messageVoiceNote": MessageType.VOICE,
        "messageAudio": MessageType.AUDIO,
        "messageVideoNote": MessageType.VIDEO_NOTE,
    }.get(
        content["@type"], MessageType.UNKNOWN
    )
    media = []
    if msg_type == MessageType.TEXT:
        body_text = content['text']['text']
    elif msg_type == MessageType.PHOTO:
        body_text = "(img) " + content['caption']['text']
        photo_size = content["photo"]["sizes"][0]
        for photo_var in content["photo"]["sizes"]:
            if photo_var["type"] == "m":  # 320x320
                photo_size = photo_var
                break
        media.append(
            MessageMedia(
                "image/png",
                photo_size['photo']['id'],
                photo_size['photo']['size'],
            )
        )
    elif msg_type == MessageType.VIDEO:
        body_text = "(vid) " + content['caption']['text']
        media.append(
            MessageMedia(
                content['video']['mime_type'],
                content['video']['video']['id'],
                content['video']['video']['size'],
            )
        )
    elif msg_type == MessageType.VOICE:
        body_text = (
            "(voice) " + time.strftime(
            '%M:%S', time.gmtime(content["voice_note"]["duration"])
        )
        )
        media.append(
            MessageMedia(
                content['voice_note']['mime_type'],
                content['voice_note']['voice']['id'],
                content['voice_note']['voice']['size'],
            )
        )
    elif msg_type == MessageType.VIDEO_NOTE:
        body_text = "(circ)"
    elif msg_type == MessageType.AUDIO:
        body_text = (
            "(audio) " + content['audio']['performer'] +
            " — " + content['audio']['title']
        )
        media.append(
            MessageMedia(
                content['audio']['mime_type'],
                content['audio']['audio']['id'],
                content['audio']['audio']['size'],
            )
        )
    else:
        body_text = "(unknown)"
    sender_id = 0
    if 'sender_id' in msg and 'user_id' in msg['sender_id']:
        sender_id = msg['sender_id']['user_id']
    in_reply_to = None
    if msg.get("reply_to") and msg["reply_to"]["@type"] == "messageReplyToMessage":
        in_reply_to = msg["reply_to"]["message_id"]
    return Message(
        id=msg["id"],
        sender_id=sender_id,
        date=datetime.fromtimestamp(msg['date']),
        type=msg_type,
        text=body_text,
        media=media,
        in_reply_to=in_reply_to,
    )


def _parse_chat(update: dict[str, Any]):
    last_msg = ""
    if msg := update.get("last_message"):
        msg = _parse_message(msg)
        last_msg = msg.text
    return Chat(
        id=update["id"],
        title=update.get("title", "Unknown"),
        unread_count=update.get("unread_count", 0),
        last_msg=last_msg
    )
