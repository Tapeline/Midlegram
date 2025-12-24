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
        if mimetype in _SUPPORTED_MIME_TYPES:
            return file_content
        raise NotImplementedError
