from abc import abstractmethod
from enum import StrEnum
from typing import NewType, Protocol

from midlegram.application.pagination import Pagination
from midlegram.domain.entities import (
    Chat,
    ChatFolder,
    ChatFolderId,
    ChatId,
    Message, MessageId, User, UserId,
)

SessionToken = NewType("SessionToken", str)


class AuthCodeVerdict(StrEnum):
    INVALID = "invalid"
    WAITING_2FA = "waiting_2fa"
    OK = "ok"


class MessengerClient(Protocol):
    @abstractmethod
    def init(self) -> None:
        raise NotImplementedError

    @abstractmethod
    async def connect_client(self) -> None:
        raise NotImplementedError

    @abstractmethod
    async def request_phone_auth(self, phone: str) -> None:
        raise NotImplementedError

    @abstractmethod
    async def is_auth_code_valid(self, code: str) -> AuthCodeVerdict:
        raise NotImplementedError

    @abstractmethod
    async def auth_with_2fa(self, password: str) -> None:
        raise NotImplementedError

    @abstractmethod
    async def get_chats_ids(
        self, folder: ChatFolderId, pagination: Pagination
    ) -> list[ChatId]:
        raise NotImplementedError

    @abstractmethod
    async def get_chat_folders(self) -> list[ChatFolder]:
        raise NotImplementedError

    @abstractmethod
    async def get_chat(self, chat_id: ChatId) -> Chat:
        raise NotImplementedError

    @abstractmethod
    async def wait_for_messages(self, timeout_s: int) -> list[Message]:
        raise NotImplementedError

    @abstractmethod
    async def get_messages(
        self,
        chat_id: ChatId,
        from_msg: MessageId,
        limit: int
    ) -> list[Message]:
        raise NotImplementedError

    @abstractmethod
    async def mark_read(
        self,
        chat_id: ChatId,
        messages: list[MessageId]
    ) -> None:
        raise NotImplementedError

    @abstractmethod
    async def send_text(self, chat_id: ChatId, text: str) -> None:
        raise NotImplementedError

    @abstractmethod
    async def get_user(self, user_id: UserId) -> User:
        raise NotImplementedError

    @abstractmethod
    def logout_and_stop(self) -> None:
        raise NotImplementedError

    @abstractmethod
    def stop(self) -> None:
        raise NotImplementedError

    @abstractmethod
    async def get_file_content(
        self, file_id: int, timeout_s: int = 300
    ) -> bytes:
        raise NotImplementedError


class ClientStore(Protocol):
    @abstractmethod
    async def new_session(self) -> SessionToken:
        raise NotImplementedError

    @abstractmethod
    async def get_client(self, tok: SessionToken) -> MessengerClient:
        raise NotImplementedError

    @abstractmethod
    async def create_client(self, tok: SessionToken) -> MessengerClient:
        raise NotImplementedError

    @abstractmethod
    async def recreate_client(self, tok: SessionToken) -> MessengerClient:
        raise NotImplementedError

    @abstractmethod
    async def logout(self, tok: SessionToken) -> None:
        raise NotImplementedError
