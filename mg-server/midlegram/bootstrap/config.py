from fuente import config_loader
from fuente.sources.env import EnvSource
from fuente.sources.yaml import YamlSource

from midlegram.config import Config

service_config_loader = config_loader(
    YamlSource("midlegram.yml"),
    EnvSource(prefix="MIDLEGRAM_", sep="__"),
    config=Config,
)