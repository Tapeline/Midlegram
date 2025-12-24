from typing import Final

from midlegram.application.exceptions import (
    ClientNotConnected,
    InvalidAuthCode, InvalidPhone, InvalidToken, NotAuthorized,
    Wrong2FAPassword,
)
from midlegram.presentation.http.framework.errors import (
    gen_handler_mapping,
    infer_code,
)
from midlegram.presentation.http.security import InvalidInstancePassword

handlers: Final = gen_handler_mapping({
    InvalidToken: infer_code,
    InvalidPhone: infer_code,
    InvalidAuthCode: infer_code,
    Wrong2FAPassword: infer_code,
    NotAuthorized: infer_code,
    ClientNotConnected: infer_code,
    InvalidInstancePassword: infer_code
})
