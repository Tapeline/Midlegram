import asyncio
from operator import attrgetter

from midlegram.application.client import ClientStore, MessengerClient
from midlegram.application.exceptions import UnknownClientError
from midlegram.application.session import SessionProvider
from midlegram.common import interactor
from midlegram.domain.entities import (
    ChatId,
    Message,
    MessageId,
    MessageWithSender, User, UserId,
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
                *map(lambda uid: _try_to_get_user_or_dummy(tg, uid),
                    map(attrgetter("sender_id"), messages))
            )
        )
        return [
            message.with_sender(sender)
            for message, sender in zip(messages, senders)
        ]


async def _try_to_get_user_or_dummy(tg: MessengerClient, uid: UserId) -> User:
    try:
        return await tg.get_user(uid)
    except UnknownClientError:
        return User(uid, f"User #{uid}", f"@#{uid}")


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
