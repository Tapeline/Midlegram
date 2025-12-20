from midlegram.application.client import ClientStore
from midlegram.application.session import SessionProvider
from midlegram.common import interactor
from midlegram.config import Config
from midlegram.domain.entities import Message


@interactor
class WaitForNewMessages:
    store: ClientStore
    session: SessionProvider
    config: Config

    async def __call__(self) -> list[Message]:
        tg = await self.store.get_client(self.session.get_token())
        return await tg.wait_for_messages(self.config.polling_timeout_s)
