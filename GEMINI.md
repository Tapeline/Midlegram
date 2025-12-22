# Project Context: SymbianGram (Telegram Client for Nokia E7)

## 1. High-Level Network Architecture
The system operates on a **Backend-for-Frontend (BFF)** architecture to bridge modern Telegram encryption with legacy hardware.

*   **Client (Nokia E7):** 
    *   Runs a **Java ME (MIDlet)** application.
    *   Connects via **HTTP/1.1 over TLS 1.2** (enabled via device patch).
    *   Acts as a "dumb terminal," rendering UI and caching data locally in RMS/FileConnection, but performing no complex cryptography or heavy processing.
*   **Backend (Middleware):**
    *   hosted on a modern server (VPS/Raspberry Pi).
    *   Maintains the persistent connection to Telegram.
    *   Handles **Media Transcoding**: Resizes images and converts video/audio to formats supported by Symbian (e.g., small JPEGs, AMR/low-bitrate MP4) before sending to the device.

## 2. Role of the Python Backend
The Python application is the core intelligence of the system.
*   **Session Management:** Manages multiple TDLib instances (one per user session), allowing multi-account support via API-driven login flows.
*   **State Machine:** Acts as the "source of truth." If the Nokia device is offline, the Backend (via TDLib) continues to receive and store messages.
*   **Protocol Translation:** Converts complex Telegram JSON objects into a lightweight custom binary format (serialization) to minimize data usage on 2G/3G networks.

## 3. Communication with Telegram
*   **Library:** The project utilizes **`python-telegram`**, a Python wrapper around the native **TDLib (Telegram Database Library)**.
*   **Mechanism:**
    *   The backend initializes the `python-telegram` client.
    *   It interfaces with the underlying TDLib binary to perform MTProto encryption and database synchronization.
    *   Login flows (Phone -> Code -> Password) are exposed programmatically to allow the Java client to authenticate remotely.

## 4. Data Retrieval Strategy

### Retrieving Chats and Folders
*   **Folders:** The backend listens for the specific `updateChatFolders` event from the `python-telegram` client and caches the folder structure in memory.
*   **Chat Lists:** 
    *   The backend queries TDLib for chat lists.
    *   It supports filtering by `folder_id` (mapping to TDLib's `chatListFolder`).
    *   It handles pagination to serve chats in small batches to the client to preserve J2ME heap memory.

### Pulling Chat History
*   **Synchronization:** 
    *   History is retrieved using TDLib's `getChatHistory` method.
    *   The backend relies on TDLib's internal SQLite database.
    *   **Fault Tolerance:** If the backend service restarts, TDLib automatically performs a difference sync with the Telegram Cloud, ensuring no messages are lost.

### Receiving New Messages
*   **Long Polling Architecture:**
    *   The Java client holds an open HTTP connection to the backend.
    *   The backend uses `asyncio` to suspend this request.
    *   An event listener attached to the `python-telegram` client waits for `updateNewMessage` or `updateChatReadInbox` events.
    *   Upon receiving an event, the backend immediately releases the HTTP response with the new data, pushing the update to the phone in near real-time.

## 5. Implementation details

### Server
Located in `mg-server`

- Binary protocol serializers are located in `midlegram/presentation/http/serializers.py`
- Telegram client impl located in `midlegram/infrastructure/tg_client.py`