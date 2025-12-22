import asyncio
from operator import attrgetter

from midlegram.application.client import ClientStore
from midlegram.application.session import SessionProvider
from midlegram.common import interactor
from midlegram.domain.entities import (
    ChatId,
    Message,
    MessageId,
    MessageWithSender,
)


@interactor
class GetMessages:
    store: ClientStore
    session: SessionProvider

    async def __call__(
        self,
        chat_id: ChatId,
        from_msg: MessageId,
        limit: int
    ) -> list[MessageWithSender]:
        tg = await self.store.get_client(self.session.get_token())
        messages = await tg.get_messages(chat_id, from_msg, limit)
        senders = list(
            await asyncio.gather(
                *map(tg.get_user,
                    map(attrgetter("sender_id"), messages))
            )
        )
        return [
            message.with_sender(sender)
            for message, sender in zip(messages, senders)
        ]


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
