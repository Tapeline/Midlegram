import asyncio
import sys
from pathlib import Path

from dishka import AsyncContainer, make_async_container
from dishka.integrations.litestar import (
    LitestarProvider,
    setup_dishka as litestar_setup_dishka
)
from litestar import Litestar
from litestar.config.cors import CORSConfig

from litestar.plugins.prometheus import PrometheusConfig, PrometheusController


from midlegram.bootstrap.di.config import ConfigDIProvider
from midlegram.bootstrap.di.tg import TgDIProvider

from midlegram.config import Config
from midlegram.bootstrap.config import service_config_loader
from midlegram.bootstrap.logging import get_structlog_plugin_def
from midlegram.presentation.http.controllers import (
    AccountController,
    ChatController,
)
from midlegram.presentation.http.errors import handlers


def _create_config() -> Config:
    return service_config_loader.load()


def _create_container(config: Config) -> AsyncContainer:
    return make_async_container(
        LitestarProvider(),
        ConfigDIProvider(),
        TgDIProvider(),
        context={
            Config: config,
        },
    )


def _select_event_loop() -> None:
    if sys.platform == "win32":
        asyncio.set_event_loop_policy(
            asyncio.WindowsSelectorEventLoopPolicy()
        )


def _init_dirs(config: Config) -> None:
    Path(config.storage.sessions_path).mkdir(parents=True, exist_ok=True)
    Path(config.storage.media_path).mkdir(parents=True, exist_ok=True)
    Path(config.storage.tdlib_path).mkdir(parents=True, exist_ok=True)


def create_app() -> Litestar:
    """Bootstrap the app."""
    _select_event_loop()
    config = _create_config()
    container = _create_container(config)
    
    prometheus_config = PrometheusConfig(
        app_name="midlegram",
        group_path=True,
        exclude=["/metrics"],
    )
    
    litestar_app = Litestar(
        debug=True,
        route_handlers=[
            AccountController,
            ChatController,
            PrometheusController,
        ],
        middleware=[
            prometheus_config.middleware,
        ],
        exception_handlers=handlers,  # type: ignore
        cors_config=CORSConfig(allow_origins=["*"]),
        plugins=[
            get_structlog_plugin_def(),
        ],
    )
    litestar_setup_dishka(container, litestar_app)
    return litestar_app
