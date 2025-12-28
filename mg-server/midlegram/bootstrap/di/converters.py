from dishka import Provider, Scope, provide, provide_all

from midlegram.application.feat_get_media import (
    AudioConverter,
    ImageConverter, VideoConverter,
)
from midlegram.infrastructure.converters import (
    FfmpegVideoConverter, PILImageConverter,
    PydubAudioConverter,
)


class ConvertersDIProvider(Provider):
    scope = Scope.APP
    image = provide(
        PILImageConverter,
        provides=ImageConverter,
    )
    audio = provide(
        PydubAudioConverter,
        provides=AudioConverter,
    )
    video = provide(
        FfmpegVideoConverter,
        provides=VideoConverter,
    )
