from midlegram.application.client import ClientStore
from midlegram.application.session import SessionProvider
from midlegram.common import interactor
from midlegram.domain.entities import ChatId


@interactor
class ConnectClient:
    store: ClientStore
    session: SessionProvider

    async def __call__(self) -> None:
        tg = await self.store.create_client(self.session.get_token())
        await tg.connect_client()
