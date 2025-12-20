from midlegram.application.client import ClientStore
from midlegram.application.session import SessionProvider
from midlegram.common import interactor
from midlegram.domain.entities import ChatId


@interactor
class SendTextMessage:
    store: ClientStore
    session: SessionProvider

    async def __call__(
        self,
        chat_id: ChatId,
        text: str
    ) -> None:
        tg = await self.store.get_client(self.session.get_token())
        await tg.send_text(chat_id, text)
