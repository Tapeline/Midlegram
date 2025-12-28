from abc import abstractmethod
from structlog import getLogger
from typing import Protocol

from midlegram.application.client import ClientStore
from midlegram.application.session import SessionProvider
from midlegram.common import interactor
from midlegram.domain.exceptions import AppError

logger = getLogger(__name__)

_SUPPORTED_MIME_TYPES = {
    "audio/mpeg",
    "audio/mp3",
    "audio/aac",
    "audio/x-ms-wma",
    "video/mp4",
    "video/x-ms-wmv",
    "image/jpeg",
    "image/jpg",
    "image/png",
    "image/gif",
    "image/bmp",
}


class ConverterNotImplemented(AppError):
    ...


class ConversionNotSuccessful(AppError):
    ...


class AudioConverter(Protocol):
    @abstractmethod
    async def convert_audio(self, content: bytes) -> bytes:
        raise NotImplementedError


class ImageConverter(Protocol):
    @abstractmethod
    async def convert_image(self, content: bytes) -> bytes:
        raise NotImplementedError


class VideoConverter(Protocol):
    @abstractmethod
    async def convert_video(self, content: bytes) -> bytes:
        raise NotImplementedError


@interactor
class GetMedia:
    store: ClientStore
    session: SessionProvider
    image_converter: ImageConverter
    audio_converter: AudioConverter
    video_converter: VideoConverter

    async def __call__(
        self,
        mimetype: str,
        file_id: int,
        timeout_s: int = 60,
    ) -> bytes:
        tg = await self.store.get_client(self.session.get_token())
        logger.info("Requesting file", file_id=file_id)
        file_content = await tg.get_file_content(file_id, timeout_s=timeout_s)
        if mimetype == "video/mp4":
            return file_content
        if mimetype.startswith("image/"):
            logger.info("Converting image")
            return await self.image_converter.convert_image(file_content)
        if mimetype.startswith("audio/"):
            logger.info("Converting audio")
            return await self.audio_converter.convert_audio(file_content)
        if mimetype.startswith("video/"):
            logger.info("Converting video")
            return await self.video_converter.convert_video(file_content)
        logger.error("Converter not implemented", mime=mimetype)
        raise ConverterNotImplemented
