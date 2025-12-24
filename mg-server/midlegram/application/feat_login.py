from midlegram.application.client import (
    AuthCodeVerdict,
    ClientStore,
    SessionToken,
)
from midlegram.application.session import SessionProvider
from midlegram.common import interactor


@interactor
class StartAuth:
    store: ClientStore

    async def __call__(self, phone: str) -> SessionToken:
        tok = await self.store.new_session()
        tg = await self.store.create_client(tok)
        await tg.request_phone_auth(phone)
        return tok


@interactor
class AuthWithCode:
    store: ClientStore
    session: SessionProvider

    async def __call__(self, code: str) -> AuthCodeVerdict:
        tg = await self.store.get_client(self.session.get_token())
        return await tg.is_auth_code_valid(code)


@interactor
class AuthWith2FA:
    store: ClientStore
    session: SessionProvider

    async def __call__(self, password: str) -> None:
        tg = await self.store.get_client(self.session.get_token())
        await tg.auth_with_2fa(password)
