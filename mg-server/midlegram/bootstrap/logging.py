import sys
from typing import Any

import structlog
from litestar.logging import StructLoggingConfig
from litestar.plugins.structlog import StructlogConfig, StructlogPlugin

shared_processors = [
    structlog.contextvars.merge_contextvars,
    structlog.processors.StackInfoRenderer(),
    structlog.processors.add_log_level,
    structlog.dev.set_exc_info,
]


def setup_processors(*, use_json: bool) -> list[Any]:
    """Setup structlog processors."""
    if use_json:
        return [
            *shared_processors,
            structlog.processors.TimeStamper(fmt="iso", utc=False),
            structlog.processors.dict_tracebacks,
            structlog.processors.JSONRenderer(),
        ]
    return [
        *shared_processors,
        structlog.processors.TimeStamper(
            fmt="%Y-%m-%d %H:%M:%S", utc=False,
        ),
        structlog.dev.ConsoleRenderer(),
    ]


def get_structlog_plugin_def(*, use_json: bool = False) -> StructlogPlugin:
    """Setup structlog plugin."""
    return StructlogPlugin(
        StructlogConfig(
            StructLoggingConfig(
                processors=setup_processors(use_json=use_json),
                logger_factory=structlog.PrintLoggerFactory(),
            ),
        ),
    )