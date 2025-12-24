from typing import Any, Final

from litestar import Request, Response
from litestar.exceptions import NotAuthorizedException
from litestar.middleware import MiddlewareProtocol
from litestar.openapi.spec import SecurityScheme
from litestar.types import ASGIApp, Receive, Scope, Send

from midlegram.application.client import SessionToken
from midlegram.application.exceptions import InvalidPhone, InvalidToken
from midlegram.application.session import SessionProvider

security_components: Final = {
    "jwt_auth": SecurityScheme(
        type="apiKey",
        name="Authorization",
        security_scheme_in="header",
    ),
}

security_defs: Final[  # noqa: WPS234
    tuple[dict[str, list[Any]], ...]
] = (
    {"jwt_auth": []},
)


def authenticate_user(
    request: Request[Any, Any, Any]
) -> SessionProvider:
    """Create user identity provider using data supplied in request."""
    auth_header = request.headers.get("authorization")
    if auth_header:
        auth_header = str(auth_header).removeprefix("Bearer").strip()
    phone_header = request.headers.get("x-phone")
    return PredefTokenProvider(auth_header, phone_header)


class PredefTokenProvider(SessionProvider):
    def __init__(self, token: SessionToken, phone: str) -> None:
        self.token = token
        self.phone = phone

    def get_token(self) -> SessionToken:
        if not self.token:
            raise InvalidToken
        return self.token

    def get_phone(self) -> str:
        if not self.phone:
            raise InvalidPhone
        return self.phone


class InvalidInstancePassword(NotAuthorizedException):
    ...


def create_instance_password_middleware(
    password: str
) -> type[MiddlewareProtocol]:
    class InstancePasswordMiddleware(MiddlewareProtocol):
        def __init__(self, app: ASGIApp) -> None:
            self.app = app
            self.secret_token = password.encode("utf-8")

        async def __call__(
            self, scope: Scope, receive: Receive, send: Send
        ) -> None:
            if scope["type"] == "http":
                headers = dict(scope["headers"])
                header_value = headers.get(b"x-password")
                if header_value != self.secret_token:
                    raise InvalidInstancePassword
            await self.app(scope, receive, send)
    return InstancePasswordMiddleware
