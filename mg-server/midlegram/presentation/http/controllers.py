import struct
from typing import Annotated, Any

from dishka import FromDishka
from dishka.integrations.litestar import inject
from litestar import Controller, Request, Response, get, post
from litestar.enums import RequestEncodingType
from litestar.params import Body, BodyKwarg
from pydantic.v1.mypy import error_from_orm

from midlegram.application.client import AuthCodeVerdict
from midlegram.application.feat_connect import ConnectClient
from midlegram.application.feat_list_chat import (
    GetChat,
    ListChatFolders,
    ListChats, ListChatsIds,
)
from midlegram.application.feat_login import (
    AuthWith2FA,
    AuthWithCode,
    StartAuth,
)
from midlegram.application.feat_poll_events import WaitForNewMessages
from midlegram.application.feat_read_messages import GetMessages, MarkRead
from midlegram.application.feat_send import SendTextMessage
from midlegram.application.pagination import Pagination
from midlegram.domain.entities import ChatFolderId, ChatId, MessageId
from midlegram.presentation.http.security import security_defs
from midlegram.presentation.http.serializers import (
    ans_ok,
    serialize_chat, serialize_chat_folder, serialize_i64, serialize_list,
    serialize_message, serialize_message_with_sender, serialize_str,
)


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
        self, chat_id: ChatId,
        request: Request[Any, Any, Any], *,
        interactor: FromDishka[SendTextMessage],
    ) -> Response[bytes]:
        msg = await request.body()
        await interactor(chat_id, msg.decode(errors="ignore"))
        return ans_ok(b"")

    @get("/updates")
    @inject
    async def poll_updates(
        self,
        *,
        interactor: FromDishka[WaitForNewMessages]
    ) -> Response[bytes]:
        messages = await interactor()
        return ans_ok(serialize_list(serialize_message, messages))

    @post("/connect")
    @inject
    async def notify_client_connected(
        self, *, interactor: FromDishka[ConnectClient]
    ) -> Response[bytes]:
        await interactor()
        return ans_ok(b"")
