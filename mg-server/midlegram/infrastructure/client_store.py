import shutil
import uuid
from abc import abstractmethod
from collections.abc import Callable
from dataclasses import dataclass, field
from pathlib import Path
from typing import NewType, Protocol

from midlegram.application.client import (
    ClientStore,
    MessengerClient,
    SessionToken,
)
from midlegram.application.exceptions import ClientNotConnected, InvalidToken
from midlegram.application.session import SessionProvider
from midlegram.config import Config


class ClientFactory(Protocol):
    @abstractmethod
    def new_client(self, session_path: Path) -> MessengerClient:
        raise NotImplementedError


@dataclass(frozen=True, slots=True)
class FSClientStore(ClientStore):
    config: Config
    client_factory: ClientFactory
    _active_sessions: dict[SessionToken, MessengerClient] = \
        field(default_factory=dict)

    async def new_session(self) -> SessionToken:
        token = str(uuid.uuid4())
        Path(self.config.storage.sessions_path, token) \
            .mkdir(parents=True, exist_ok=True)
        return SessionToken(token)

    async def get_client(self, tok: SessionToken) -> MessengerClient:
        if tok not in self._active_sessions:
            raise ClientNotConnected
        return self._active_sessions[tok]

    async def create_client_for_login(
        self,
        tok: SessionToken
    ) -> MessengerClient:
        session_path = Path(self.config.storage.sessions_path, tok)
        if not session_path.exists():
            raise InvalidToken
        client = self.client_factory.new_client(session_path)
        client.init()
        self._active_sessions[tok] = client
        return client

    async def create_client(self, tok: SessionToken) -> MessengerClient:
        if tok in self._active_sessions:
            return await self.get_client(tok)
        session_path = Path(self.config.storage.sessions_path, tok)
        if not session_path.exists():
            raise InvalidToken
        client = self.client_factory.new_client(session_path)
        client.init()
        await client.connect_client()
        self._active_sessions[tok] = client
        return client

    async def recreate_client(self, tok: SessionToken) -> MessengerClient:
        if tok in self._active_sessions:
            client = await self.get_client(tok)
            client.stop()
            self._active_sessions.pop(tok)
        return await self.create_client(tok)

    async def logout(self, tok: SessionToken) -> None:
        if client := self._active_sessions.get(tok):
            client.logout_and_stop()
            self._active_sessions.pop(tok)
            shutil.rmtree(Path(self.config.storage.sessions_path, tok))
