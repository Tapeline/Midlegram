import asyncio

from midlegram.application.client import ClientStore
from midlegram.application.pagination import Pagination
from midlegram.application.session import SessionProvider
from midlegram.common import dto, interactor
from midlegram.domain.entities import Chat, ChatFolder, ChatFolderId


@interactor
class ListChats:
    store: ClientStore
    session: SessionProvider

    async def __call__(
        self,
        folder: ChatFolderId,
        pagination: Pagination
    ) -> list[Chat]:
        tg = await self.store.get_client(self.session.get_token())
        await asyncio.sleep(2)
        chats_ids = await tg.get_chats_ids(folder, pagination)
        return list(await asyncio.gather(*map(tg.get_chat, chats_ids)))


@interactor
class ListChatFolders:
    store: ClientStore
    session: SessionProvider

    async def __call__(self) -> list[ChatFolder]:
        tg = await self.store.get_client(self.session.get_token())
        return await tg.get_chat_folders()
