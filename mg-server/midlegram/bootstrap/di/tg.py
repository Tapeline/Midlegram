from dishka import Provider, Scope, provide, provide_all

from litestar import Request

from midlegram.application.client import ClientStore
from midlegram.application.feat_connect import ConnectClient, ReconnectClient
from midlegram.application.feat_get_media import GetMedia
from midlegram.application.feat_list_chat import (
    GetChat,
    ListChatFolders,
    ListChats, ListChatsIds, SearchChats,
)
from midlegram.application.feat_login import (
    AuthWith2FA,
    AuthWithCode,
    StartAuth,
)
from midlegram.application.feat_poll_events import (
    WaitForNewMessages,
    WaitForNewMessagesInChat,
)
from midlegram.application.feat_read_messages import GetMessages, MarkRead
from midlegram.application.feat_send import SendFileMessage, SendTextMessage
from midlegram.application.session import SessionProvider
from midlegram.config import Config
from midlegram.infrastructure.client_store import ClientFactory, FSClientStore
from midlegram.infrastructure.tg_client import DefaultTgClientFactory
from midlegram.presentation.http.security import authenticate_user


class TgDIProvider(Provider):
    factory = provide(
        DefaultTgClientFactory,
        provides=ClientFactory,
        scope=Scope.APP
    )

    interactors = provide_all(
        StartAuth,
        AuthWithCode,
        AuthWith2FA,
        ListChatFolders,
        ListChats,
        GetMessages,
        SendTextMessage,
        MarkRead,
        WaitForNewMessages,
        ConnectClient,
        GetChat,
        ListChatsIds,
        ReconnectClient,
        GetMedia,
        SearchChats,
        SendFileMessage,
        WaitForNewMessagesInChat,
        scope=Scope.REQUEST,
    )

    @provide(scope=Scope.APP)
    def provide_store(
        self,
        config: Config,
        factory: ClientFactory,
    ) -> ClientStore:
        return FSClientStore(
            config=config, client_factory=factory
        )

    @provide(scope=Scope.REQUEST)
    def provide_identity(
        self,
        request: Request,  # type: ignore
    ) -> SessionProvider:
        return authenticate_user(request)
