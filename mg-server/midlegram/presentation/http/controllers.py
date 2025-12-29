import struct
from typing import Any, AsyncGenerator

from dishka import FromDishka
from dishka.integrations.litestar import inject
from litestar import Controller, Request, Response, get, post
from litestar.response import Stream
from structlog import getLogger

from midlegram.application.client import AuthCodeVerdict
from midlegram.application.feat_connect import ConnectClient, ReconnectClient
from midlegram.application.feat_get_media import GetMedia
from midlegram.application.feat_list_chat import (
    GetChat,
    ListChatFolders,
    ListChats, ListChatsIds, SearchChats,
)
from midlegram.application.feat_login import (
    AuthWith2FA,
    AuthWithCode,
    StartAuth,
)
from midlegram.application.feat_poll_events import (
    WaitForNewMessages,
    WaitForNewMessagesInChat,
)
from midlegram.application.feat_read_messages import GetMessages, MarkRead
from midlegram.application.feat_send import SendFileMessage, SendTextMessage
from midlegram.application.pagination import Pagination
from midlegram.domain.entities import ChatFolderId, ChatId, MessageId
from midlegram.presentation.http.security import security_defs
from midlegram.presentation.http.serializers import (
    ans_ok,
    serialize_chat, serialize_chat_folder, serialize_i64, serialize_list,
    serialize_message, serialize_message_with_sender, serialize_str,
)

logger = getLogger(__name__)


class AccountController(Controller):
    path = "/api/account"

    @post("/login/phone")
    @inject
    async def login_phone(
        self,
        phone: str, *,
        interactor: FromDishka[StartAuth]
    ) -> Response[bytes]:
        return ans_ok(serialize_str(await interactor(phone)))

    @post("/login/code", security=security_defs)
    @inject
    async def login_code(
        self,
        code: str, *,
        interactor: FromDishka[AuthWithCode]
    ) -> Response[bytes]:
        verdict = await interactor(code)
        code = {
            AuthCodeVerdict.OK: 0,
            AuthCodeVerdict.WAITING_2FA: 1,
            AuthCodeVerdict.INVALID: 2,
        }.get(verdict)
        return ans_ok(struct.pack(">b", code))

    @post("/login/2fa", security=security_defs)
    @inject
    async def login_2fa(
        self,
        password: str, *,
        interactor: FromDishka[AuthWith2FA]
    ) -> Response[bytes]:
        await interactor(password)
        return ans_ok(b"")


class ChatController(Controller):
    security = security_defs
    path = "/api"

    @get("/folders")
    @inject
    async def get_folders(
        self,
        *,
        interactor: FromDishka[ListChatFolders]
    ) -> Response[bytes]:
        folders = await interactor()
        return ans_ok(serialize_list(serialize_chat_folder, folders))

    @get("/folders/{folder_id:int}/chats")
    @inject
    async def get_chats(
        self, folder_id: ChatFolderId, limit: int = 10, offset: int = 0, *,
        interactor: FromDishka[ListChats]
    ) -> Response[bytes]:
        chats = await interactor(folder_id, Pagination(limit, offset))
        return ans_ok(serialize_list(serialize_chat, chats))

    @get("/folders/{folder_id:int}/chats_ids")
    @inject
    async def get_chats_ids(
        self, folder_id: ChatFolderId, limit: int = 10, offset: int = 0, *,
        interactor: FromDishka[ListChatsIds]
    ) -> Response[bytes]:
        chats = await interactor(folder_id, Pagination(limit, offset))
        return ans_ok(serialize_list(serialize_i64, chats))

    @get("/chats/{chat_id:int}")
    @inject
    async def get_chat(
        self, chat_id: ChatId, *,
        interactor: FromDishka[GetChat]
    ) -> Response[bytes]:
        chat = await interactor(chat_id)
        return ans_ok(serialize_chat(chat))

    @get("/chats/{chat_id:int}/messages")
    @inject
    async def get_messages(
        self, chat_id: ChatId, from_msg: MessageId, lim: int, *,
        interactor: FromDishka[GetMessages]
    ) -> Response[bytes]:
        messages = await interactor(chat_id, from_msg, lim)
        return ans_ok(serialize_list(serialize_message_with_sender, messages))

    @post("/chats/{chat_id:int}/read")
    @inject
    async def read_messages(
        self, chat_id: ChatId, m: list[MessageId], *,
        interactor: FromDishka[MarkRead],
    ) -> Response[bytes]:
        await interactor(chat_id, m)
        return ans_ok(b"")

    @post("/chats/{chat_id:int}/send/text")
    @inject
    async def send_message(
        self, *,
        request: Request[Any, Any, Any],
        chat_id: ChatId,
        reply: MessageId | None = None,
        interactor: FromDishka[SendTextMessage],
    ) -> Response[bytes]:
        msg = await request.body()
        logger.info("Sending message", bytes=[int(c) for c in msg])
        await interactor(chat_id, reply, msg.decode(errors="ignore"))
        return ans_ok(b"")

    @post("/chats/{chat_id:int}/send/file/{media_type:str}")
    @inject
    async def send_file_message(
        self, *,
        request: Request[Any, Any, Any],
        chat_id: ChatId,
        media_type: str,
        reply: MessageId | None = None,
        interactor: FromDishka[SendFileMessage],
    ) -> Response[bytes]:
        msg = await request.body()
        await interactor(chat_id, reply, media_type, msg)
        return ans_ok(b"")

    @get("/updates")
    @inject
    async def poll_updates(
        self,
        *,
        t: int = 30,
        interactor: FromDishka[WaitForNewMessages]
    ) -> Response[bytes]:
        messages = await interactor(polling_timeout_s=t)
        return ans_ok(serialize_list(serialize_message_with_sender, messages))

    @get("/chats/{chat_id:int}/updates")
    @inject
    async def poll_updates_in_chat(
        self,
        *,
        chat_id: ChatId,
        t: int = 30,
        interactor: FromDishka[WaitForNewMessagesInChat]
    ) -> Stream:
        async def update_stream() -> AsyncGenerator[bytes, None]:
            yield b"\xAA"
            messages = await interactor(polling_timeout_s=t, chat_id=chat_id)
            yield ans_ok(
                serialize_list(serialize_message_with_sender, messages)
            ).content
        return Stream(
            update_stream(),
            media_type="application/octet-stream",
            status_code=200,
            headers={"X-Accel-Buffering": "no"}
        )

    @post("/connect")
    @inject
    async def connect(
        self, *, interactor: FromDishka[ConnectClient]
    ) -> Response[bytes]:
        await interactor()
        return ans_ok(b"")

    @post("/reconnect")
    @inject
    async def reconnect(
        self, *, interactor: FromDishka[ReconnectClient]
    ) -> Response[bytes]:
        await interactor()
        return ans_ok(b"")

    @get("/file/{file_id:int}")
    @inject
    async def get_file(
        self, *,
        file_id: int,
        mime: str,
        timeout: int = 60,
        interactor: FromDishka[GetMedia]
    ) -> Response[bytes]:
        return Response(
            await interactor(mime, file_id, timeout),
            headers={"Content-Type": mime},
        )

    @get("/chats/search")
    @inject
    async def search_chats(
        self, *,
        q: str,
        limit: int = 10,
        interactor: FromDishka[SearchChats]
    ) -> Response[bytes]:
        chats = await interactor(q, limit)
        return ans_ok(serialize_list(serialize_chat, chats))
