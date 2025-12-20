from dishka import Provider, Scope, from_context, provide

from midlegram.config import (
    LoggingConfig,
    Config,
)


class ConfigDIProvider(Provider):
    """Provider of configs."""

    config = from_context(Config, scope=Scope.APP)

    
    @provide(scope=Scope.APP)
    def get_logging_config(self, config: Config) -> LoggingConfig:
        """Provide logging conf."""
        return config.logging
    