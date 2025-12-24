from typing import Any

from midlegram.common import dto
from midlegram.domain.exceptions import AppError


class InvalidToken(AppError):
    ...


class InvalidPhone(AppError):
    ...


class InvalidAuthCode(AppError):
    ...


class Wrong2FAPassword(AppError):
    ...


class NotAuthorized(AppError):
    ...


class ClientNotConnected(AppError):
    ...


class TelegramSessionExpired(AppError):
    ...


@dto
class UnknownClientError(AppError):
    detail: dict[str, Any]
