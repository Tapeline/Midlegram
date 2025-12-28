import io

from pydub import AudioSegment

from midlegram.application.client import ClientStore
from midlegram.application.session import SessionProvider
from midlegram.common import interactor
from midlegram.domain.entities import ChatId, MessageId


@interactor
class SendTextMessage:
    store: ClientStore
    session: SessionProvider

    async def __call__(
        self,
        chat_id: ChatId,
        reply_to: MessageId | None,
        text: str
    ) -> None:
        tg = await self.store.get_client(self.session.get_token())
        await tg.send_text(chat_id, reply_to, text)


@interactor
class SendFileMessage:
    store: ClientStore
    session: SessionProvider

    async def __call__(
        self,
        chat_id: ChatId,
        reply_to: MessageId | None,
        media_type: str,
        contents: bytes,
    ) -> None:
        tg = await self.store.get_client(self.session.get_token())
        if media_type == "voice_note":
            audio = AudioSegment.from_file(io.BytesIO(contents))
            output = io.BytesIO()
            audio.export(output, format="ogg", codec="opus")
            contents = output.getvalue()
        await tg.send_file(chat_id, reply_to, media_type, contents)
