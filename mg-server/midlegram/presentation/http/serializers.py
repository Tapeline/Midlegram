import struct

from litestar import Response

from midlegram.domain.entities import Chat, ChatFolder, Message, MessageType


def ans_ok(data: bytes) -> Response[bytes]:
    return Response(
        b"\x00" + data,
        headers={"Content-Type": "application/octet-stream"},
    )


def ans_err(err_code: str) -> Response[bytes]:
    return Response(
        b"\xFF" + serialize_str(err_code),
        headers={"Content-Type": "application/octet-stream"},
    )


def serialize_str(text: str) -> bytes:
    encoded = text.encode("utf8")
    return struct.pack(">h", len(encoded)) + encoded


def serialize_chat_folder(folder: ChatFolder) -> bytes:
    return struct.pack(">q", folder.id) + serialize_str(folder.title)


_MSG_TYPE_MAP = {
    MessageType.TEXT: 0,
    MessageType.PHOTO: 1,
    MessageType.VIDEO: 2,
    MessageType.VOICE: 3,
    MessageType.UNKNOWN: 4,
}


def serialize_message(message: Message) -> bytes:
    return struct.pack(
        ">qbiq",
        message.id,
        _MSG_TYPE_MAP[message.type],
        int(message.date.timestamp()),
        message.sender.id,
    ) + serialize_str(message.text)


def serialize_chat(chat: Chat) -> bytes:
    return (
        struct.pack(">q", chat.id)
        + serialize_str(chat.title)
        + struct.pack(
            ">i?",
            chat.unread_count,
            chat.last_msg is not None,
        )
        + (serialize_str(chat.last_msg) if chat.last_msg is not None else b'')
    )


def serialize_list(serializer, list) -> bytes:
    return struct.pack(">i", len(list)) + b"".join(
        serializer(item) for item in list
    )
