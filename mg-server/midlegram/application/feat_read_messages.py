from midlegram.application.client import ClientStore
from midlegram.application.session import SessionProvider
from midlegram.common import interactor
from midlegram.domain.entities import ChatId, Message, MessageId


@interactor
class GetMessages:
    store: ClientStore
    session: SessionProvider

    async def __call__(
        self,
        chat_id: ChatId,
        from_msg: MessageId,
        limit: int
    ) -> list[Message]:
        tg = await self.store.get_client(self.session.get_token())
        return await tg.get_messages(chat_id, from_msg, limit)


@interactor
class MarkRead:
    store: ClientStore
    session: SessionProvider

    async def __call__(
        self,
        chat_id: ChatId,
        messages: list[MessageId],
    ) -> None:
        tg = await self.store.get_client(self.session.get_token())
        await tg.mark_read(chat_id, messages)
