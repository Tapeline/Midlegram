import struct

from dishka import FromDishka
from dishka.integrations.litestar import inject
from litestar import Controller, Response, get, post

from midlegram.application.client import AuthCodeVerdict
from midlegram.application.feat_list_chat import ListChatFolders, ListChats
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
    serialize_chat, serialize_chat_folder, serialize_list,
    serialize_message, serialize_str,
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
        breakpoint()
        return ans_ok(serialize_str(await interactor(phone)))

    @post("/login/code", security=security_defs)
    @inject
    async def login_code(
        self,
        code: str, *,
        interactor: FromDishka[AuthWithCode]
    ) -> Response[bytes]:
        breakpoint()
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
        breakpoint()
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
        breakpoint()
        folders = await interactor()
        return ans_ok(serialize_list(serialize_chat_folder, folders))

    @get("/folders/{folder_id:int}/chats")
    @inject
    async def get_chats(
        self, folder_id: ChatFolderId, *,
        interactor: FromDishka[ListChats]
    ) -> Response[bytes]:
        chats = await interactor(folder_id, Pagination(0))
        return ans_ok(serialize_list(serialize_chat, chats))

    @get("/chats/{chat_id:int}/messages")
    @inject
    async def get_messages(
        self, chat_id: ChatId, from_msg: MessageId, lim: int, *,
        interactor: FromDishka[GetMessages]
    ) -> Response[bytes]:
        messages = await interactor(chat_id, from_msg, lim)
        return ans_ok(serialize_list(serialize_message, messages))

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
    async def read_messages(
        self, chat_id: ChatId,
        text: str, *,
        interactor: FromDishka[SendTextMessage],
    ) -> Response[bytes]:
        await interactor(chat_id, text)
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
