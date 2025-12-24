from PIL import Image
import io

from pydub import AudioSegment

from midlegram.application.client import ClientStore
from midlegram.application.exceptions import UnknownClientError
from midlegram.application.session import SessionProvider
from midlegram.common import interactor


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


@interactor
class GetMedia:
    store: ClientStore
    session: SessionProvider

    async def __call__(
        self,
        mimetype: str,
        file_id: int,
        timeout_s: int = 60,
    ) -> bytes:
        tg = await self.store.get_client(self.session.get_token())
        file_content = await tg.get_file_content(file_id, timeout_s=timeout_s)
        if mimetype.startswith("image/"):
            return _convert_image(file_content)
        if mimetype.startswith("audio/"):
            return _convert_audio(file_content)
        if mimetype in _SUPPORTED_MIME_TYPES:
            return file_content
        raise NotImplementedError


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
