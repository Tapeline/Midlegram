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
    sessions_path: str = ".store/sessions"
    media_path: str = ".store/media"
    tdlib_path: str = ".store/tdlib"


@dataclass
class Config:
    tg_app: TgAppConfig
    storage: StorageConfig
    logging: LoggingConfig
    max_msg_queue_size: int = 200
    instance_password: str | None = None
