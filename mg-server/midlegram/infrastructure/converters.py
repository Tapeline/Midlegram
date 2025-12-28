import asyncio
import io
import uuid
from dataclasses import dataclass
from pathlib import Path

from PIL import Image
from pydub import AudioSegment
import ffmpeg
from structlog import getLogger

from midlegram.application.feat_get_media import (
    AudioConverter,
    ConversionNotSuccessful, ImageConverter,
    VideoConverter,
)
from midlegram.config import Config

logger = getLogger(__name__)


class PILImageConverter(ImageConverter):
    async def convert_image(self, content: bytes) -> bytes:
        return _convert_image(content)


class PydubAudioConverter(AudioConverter):
    async def convert_audio(self, content: bytes) -> bytes:
        return await asyncio.to_thread(_convert_image, content)


@dataclass
class FfmpegVideoConverter(VideoConverter):
    config: Config

    async def convert_video(self, content: bytes) -> bytes:
        return await asyncio.to_thread(
            _convert_video, content, self.config.storage.media_path
        )


def _convert_image(image_data: bytes) -> bytes:
    image = Image.open(io.BytesIO(image_data))
    output = io.BytesIO()
    image.save(output, "PNG")
    return output.getvalue()


def _convert_audio(audio_data: bytes) -> bytes:
    audio = AudioSegment.from_file(io.BytesIO(audio_data))
    output = io.BytesIO()
    audio.export(output, format="adts", codec="aac")
    return output.getvalue()


def _convert_video(content: bytes, media_path: str) -> bytes:
    video_id = str(uuid.uuid4())
    input_path = Path(media_path, f"{video_id}")
    input_path.write_bytes(content)
    output_path = Path(media_path, f"{video_id}.mp4")
    try:
        ffmpeg \
            .input(input_path) \
            .output(output_path, vcodec='libx264', acodec='aac') \
            .overwrite_output() \
            .run(quiet=True)
        converted = output_path.read_bytes()
        input_path.unlink(missing_ok=True)
        output_path.unlink(missing_ok=True)
        return converted
    except ffmpeg.Error as e:
        logger.exception("Error: ", e.stderr.decode())
        raise ConversionNotSuccessful
