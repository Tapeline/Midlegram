from typing import Any, Final

from litestar import Request
from litestar.openapi.spec import SecurityScheme

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
