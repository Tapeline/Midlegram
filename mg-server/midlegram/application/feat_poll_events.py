import asyncio
from operator import attrgetter

from midlegram.application.client import ClientStore, MessengerClient
from midlegram.application.exceptions import UnknownClientError
from midlegram.application.session import SessionProvider
from midlegram.common import interactor
from midlegram.config import Config
from midlegram.domain.entities import Message, MessageWithSender, User, UserId


@interactor
class WaitForNewMessages:
    store: ClientStore
    session: SessionProvider
    config: Config

    async def __call__(
        self, polling_timeout_s: int
    ) -> list[MessageWithSender]:
        tg = await self.store.get_client(self.session.get_token())
        messages = await tg.wait_for_messages(polling_timeout_s)
        senders = list(
            await asyncio.gather(
                *map(
                    lambda uid: _try_to_get_user_or_dummy(tg, uid),
                    map(attrgetter("sender_id"), messages)
                )
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
