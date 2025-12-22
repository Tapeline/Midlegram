from datetime import datetime
from enum import StrEnum
from typing import NewType

from midlegram.common import dto, entity

ChatFolderId = NewType("ChatFolderId", int)


@entity
class ChatFolder:
    id: ChatFolderId
    title: str


ChatId = NewType("ChatId", int)


@entity
class Chat:
    id: ChatId
    title: str
    unread_count: int
    last_msg: str | None


MessageId = NewType("MessageId", int)
MediaId = NewType("MediaId", int)
UserId = NewType("UserId", int)


@entity
class User:
    id: UserId
    name: str
    handle: str


class MessageType(StrEnum):
    TEXT = "text"
    PHOTO = "photo"
    VIDEO = "video"
    VOICE = "voice"
    UNKNOWN = "unknown"


@entity
class Message:
    id: MessageId
    type: MessageType
    sender_id: UserId
    date: datetime
    text: str
    linked_media: list[MediaId]

    def with_sender(self, user: User) -> "MessageWithSender":
        return MessageWithSender(
            id=self.id,
            type=self.type,
            sender=user,
            date=self.date,
            text=self.text,
            linked_media=self.linked_media,
        )


@entity
class MessageWithSender:
    id: MessageId
    type: MessageType
    sender: User
    date: datetime
    text: str
    linked_media: list[MediaId]
