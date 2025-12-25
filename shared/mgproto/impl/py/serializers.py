import struct


def serialize_id(id: int) -> bytes:
    return struct.pack(">q", id)


def serialize_int(value: int) -> bytes:
    return struct.pack(">i", value)


def serialize_string_length(length: int) -> bytes:
    return struct.pack(">h", length)


def serialize_string(string: str) -> bytes:
    string_bytes = string.encode("utf-8")
    length = len(string_bytes)
    return serialize_string_length(length) + string_bytes


def serialize_chat_folder(chat_folder: ChatFolder) -> bytes:
    return (
        serialize_id(chat_folder.id)
        + serialize_string(chat_folder.title)
    )


def serialize_chat(chat: Chat) -> bytes:
    return (
        serialize_id(chat.id)
        + serialize_string(chat.title)
        + serialize_int(chat.unread_count)
        + struct.pack(">?", chat.has_last_message)
        + serialize_string(chat.last_message)
    )


def serialize_message_type(message_type: MessageType) -> bytes:
    return struct.pack(">b", message_type)


def serialize_message(message: Message) -> bytes:
    return (
        serialize_id(message.id)
        + serialize_message_type(message.type)
        + serialize_int(message.timestamp)
        + serialize_id(message.sender_id)
        + serialize_string(message.sender_name)
        + serialize_string(message.sender_handle)
        + serialize_string(message.text)
    )


def serialize_message_list(message_list: MessageList) -> bytes:
    return (
        serialize_int(message_list.length)
        + b"".join(
            serialize_message(message) for message in message_list.messages
        )
    )


def serialize_chat_list(chat_list: ChatList) -> bytes:
    return (
        serialize_int(chat_list.length)
        + b"".join(serialize_chat(chat) for chat in chat_list.chats)
    )


def serialize_chat_folder_list(chat_folder_list: ChatFolderList) -> bytes:
    return (
        serialize_int(chat_folder_list.length)
        + b"".join(
            serialize_chat_folder(chat_folder)
            for chat_folder in chat_folder_list.chat_folders
        )
    )


def serialize_response_status(status: ResponseStatus) -> bytes:
    return struct.pack(">b", status)


def serialize_response(response: Response) -> bytes:
    return (
        serialize_response_status(response.status)
        + response.data
    )