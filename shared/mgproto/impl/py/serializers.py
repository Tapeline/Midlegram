import struct
# following type_declarations attribute implement serialization 
# of given types using python's struct library
# use ">" parameter (big-endian option) when serializing with struct
# define a python function for each type that needs to be serialized:
# def serialize_$typename(parameters declared for specified type) -> bytes:
#     serialization...
# respect python's name convention: convert any camelCase to snake_case

def serialize_id(value: int) -> bytes:
    return struct.pack(">q", value)

def serialize_int(value: int) -> bytes:
    return struct.pack(">i", value)

def serialize_string_length(value: int) -> bytes:
    return struct.pack(">h", value)

def serialize_string(text: str) -> bytes:
    text_bytes = text.encode('utf-8')
    length = len(text_bytes)
    return serialize_string_length(length) + text_bytes

def serialize_chat_folder(id: int, title: str) -> bytes:
    return serialize_id(id) + serialize_string(title)

def serialize_chat(id: int, title: str, unread_count: int, has_last_message: bool, last_message: str) -> bytes:
    return serialize_id(id) + serialize_string(title) + serialize_int(unread_count) + struct.pack(">?", has_last_message) + serialize_string(last_message)

def serialize_message_type(msg_type: int) -> bytes:
    return struct.pack(">b", msg_type)

def serialize_message(id: int, type: int, timestamp: int, sender_id: int, sender_name: str, sender_handle: str, text: str) -> bytes:
    return serialize_id(id) + serialize_message_type(type) + serialize_int(timestamp) + serialize_id(sender_id) + serialize_string(sender_name) + serialize_string(sender_handle) + serialize_string(text)

def serialize_message_list(messages: list) -> bytes:
    length = len(messages)
    serialized_messages = b''.join(messages)
    return serialize_int(length) + serialized_messages

def serialize_chat_list(chats: list) -> bytes:
    length = len(chats)
    serialized_chats = b''.join(chats)
    return serialize_int(length) + serialized_chats

def serialize_chat_folder_list(chat_folders: list) -> bytes:
    length = len(chat_folders)
    serialized_chat_folders = b''.join(chat_folders)
    return serialize_int(length) + serialized_chat_folders

def serialize_response_status(is_ok: bool) -> bytes:
    status = 0x00 if is_ok else 0xFF
    return struct.pack(">b", status)

def serialize_response(is_ok: bool, response_data: bytes) -> bytes:
    return serialize_response_status(is_ok) + response_data
