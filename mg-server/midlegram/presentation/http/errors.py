from typing import Final

from midlegram.application.exceptions import (
    ClientNotConnected,
    InvalidAuthCode, InvalidPhone, InvalidToken, NotAuthorized,
    TelegramSessionExpired, UnknownMediaType, Wrong2FAPassword,
)
from midlegram.application.feat_get_media import (
    ConversionNotSuccessful,
    ConverterNotImplemented,
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
    InvalidInstancePassword: (401, infer_code),
    TelegramSessionExpired: (401, infer_code),
    ConverterNotImplemented: (501, infer_code),
    ConversionNotSuccessful: (500, infer_code),
    UnknownMediaType: (400, infer_code),
})
