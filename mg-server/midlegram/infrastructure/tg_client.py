from asyncio import Queue
from contextlib import suppress
from dataclasses import dataclass, field
import asyncio
from datetime import datetime

from structlog import BoundLogger, getLogger
from pathlib import Path
from typing import Any

from telegram.client import Telegram
from telegram.utils import AsyncResult

from midlegram.application.client import AuthCodeVerdict, MessengerClient
from midlegram.application.exceptions import (
    InvalidAuthCode,
    UnknownClientError,
    Wrong2FAPassword,
)
from midlegram.application.pagination import Pagination
from midlegram.config import Config
from midlegram.domain.entities import (
    Chat,
    ChatFolder,
    ChatFolderId, ChatId,
    Message,
    MessageId, MessageType, Sender,
)
from midlegram.infrastructure.client_store import ClientFactory

logger = getLogger(__name__)
_MAX_CHATS: int = 1000


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
        logger.error("Unknown client error: %s", result.error_info)
        raise UnknownClientError(result.error_info)
    return result


@dataclass(slots=True)
class TelegramClient(MessengerClient):
    config: Config
    tg: Telegram
    _listeners: list[Queue[Message]] = field(default_factory=list)
    _folders: list[ChatFolder] = field(default_factory=list)
    _folder_chat_ids: dict[ChatFolderId, list[ChatId]] = field(default_factory=dict)
    _chats_in_folders: dict[ChatFolderId, list[Chat]] = field(default_factory=dict)
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
        self.tg.call_method('getOption', {'name': 'version'}).wait()

    def _put_new_msg(self, update: dict[str, Any]) -> None:
        #logger.debug("Got a message", msg=update["message"])
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

    async def connect_client(self) -> None:
        self._chats.clear()
        await asyncio.sleep(2)
        ensure_no_error(await wait_tg(
            self.tg.call_method(
                'loadChats', {
                    'chat_list': {'@type': 'chatListMain'},
                    'limit': _MAX_CHATS,
                }
            )
        ))
        all_chats = ensure_no_error(
            await wait_tg(self.tg.get_chats(limit=_MAX_CHATS))
        ).update["chat_ids"]
        self._chats_in_folders[ChatFolderId(0)] = list(
            await asyncio.gather(*map(self._load_chat, all_chats))
        )
        for chats in self._chats_in_folders.values():
            for chat in chats:
                self._chats[chat.id] = chat
        for folder in self._folders:
            chat_ids = ensure_no_error(await wait_tg(
                self.tg.call_method(
                    'getChats', {
                        'chat_list': {
                            '@type': 'chatListFolder',
                            'chat_folder_id': folder.id
                        },
                        'limit': _MAX_CHATS,
                    }
                )
            )).update["chat_ids"]
            self._folder_chat_ids[folder.id] = chat_ids
            self._chats_in_folders[folder.id] = [
                self._chats[chat_id] for chat_id in chat_ids
            ]

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
        return self._folder_chat_ids.get(folder_id, [])

    async def get_chat_folders(self) -> list[ChatFolder]:
        return self._folders

    async def get_chat(self, chat_id: ChatId) -> Chat:
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

    async def send_text(self, chat_id: ChatId, text: str) -> None:
        logger.info("Sending text", chat_id=chat_id)
        ensure_no_error(
            await wait_tg(
                self.tg.call_method(
                    'sendMessage', {
                        'chat_id': chat_id,
                        'input_message_content': {
                            '@type': 'inputMessageText',
                            'text': {'text': text}
                        }
                    }
                )
            )
        )

    def logout_and_stop(self) -> None:
        logger.info("Logging out")
        self.tg.call_method('logOut', {})
        self.tg.stop()


def _parse_message(msg: dict[str, Any]) -> Message:
    content = msg["content"]
    msg_type = {
        "messageText": MessageType.TEXT,
        "messagePhoto": MessageType.PHOTO,
        "messageVideo": MessageType.VIDEO,
        "messageVoiceNote": MessageType.VOICE,
    }.get(
        content["@type"], MessageType.UNKNOWN
    )
    if msg_type == MessageType.TEXT:
        body_text = content['text']['text']
    elif msg_type == MessageType.PHOTO:
        body_text = "(img) " + content['caption']['text']
    elif msg_type == MessageType.VIDEO:
        body_text = "(vid) " + content['caption']['text']
    elif msg_type == MessageType.VOICE:
        body_text = "(voice)"
    else:
        body_text = "(unknown)"
    sender_id = 0
    if 'sender_id' in msg and 'user_id' in msg['sender_id']:
        sender_id = msg['sender_id']['user_id']
    return Message(
        id=msg["id"],
        sender=Sender(id=sender_id, name=str(sender_id)),
        date=datetime.fromtimestamp(msg['date']),
        type=msg_type,
        text=body_text,
        linked_media=[]
    )


def _parse_chat(update: dict[str, Any]):
    if last_msg := update.get("last_message"):
        if last_msg["content"]["@type"] == "messageText":
            last_msg = last_msg["content"]["text"]["text"]
        elif last_msg["content"]["@type"] == "messagePhoto":
            last_msg = "(img) " + last_msg["content"]["caption"]["text"]
        elif last_msg["content"]["@type"] == "messageVideo":
            last_msg = "(vid) " + last_msg["content"]["caption"]["text"]
        elif last_msg["content"]["@type"] == "messageVoiceNote":
            last_msg = "(voice)"
        elif last_msg["content"]["@type"] == "messageAudio":
            last_msg = "(audio)"
        else:
            last_msg = "(unknown)"
    return Chat(
        id=update["id"],
        title=update.get("title", "Unknown"),
        unread_count=update.get("unread_count", 0),
        last_msg=last_msg
    )
