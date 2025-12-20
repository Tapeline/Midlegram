from dataclasses import dataclass


@dataclass(frozen=True, slots=True)
class LoggingConfig:
    """Logging config."""

    use_json: bool = False


@dataclass
class TgAppConfig:
    api_id: int
    api_hash: str
    dc_ip: str


@dataclass
class StorageConfig:
    encryption_key: str
    sessions_path: str = ".sessions"
    media_path: str = ".media"
    tdlib_path: str = ".tdlib"


@dataclass
class Config:
    tg_app: TgAppConfig
    storage: StorageConfig
    logging: LoggingConfig
    polling_timeout_s: int = 30
    max_msg_queue_size: int = 50
