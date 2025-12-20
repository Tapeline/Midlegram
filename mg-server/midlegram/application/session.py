from abc import abstractmethod
from typing import Protocol

from midlegram.application.client import SessionToken


class SessionProvider(Protocol):
    @abstractmethod
    def get_token(self) -> SessionToken:
        raise NotImplementedError

    @abstractmethod
    def get_phone(self) -> str:
        raise NotImplementedError
