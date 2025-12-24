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
    InvalidToken: (401, infer_code),
    InvalidPhone: (401, infer_code),
    InvalidAuthCode: (401, infer_code),
    Wrong2FAPassword: (401, infer_code),
    NotAuthorized: (401, infer_code),
    ClientNotConnected: (409, infer_code),
    InvalidInstancePassword: (401, infer_code)
})
