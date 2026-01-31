package midp.tapeline.midlegram.client;

import midp.tapeline.midlegram.state.data.Chat;
import midp.tapeline.midlegram.state.data.PaginatedChats;

import javax.microedition.io.HttpConnection;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public interface TelegramClient {

    String getSessionKey();

    void startAuth(String phone) throws IOException;

    void confirmCode(String code) throws IOException;

    Vector getFolders() throws IOException;

    Vector getChatsIds(long folderId) throws IOException;

    PaginatedChats getChats(long folderId, int page) throws IOException;

    Chat getChat(long chatId) throws IOException;

    Vector getMessages(long chatId, long fromMsgId, int count) throws IOException;

    void sendTextMessage(long chatId, long replyTo, String message) throws IOException;

    Vector searchChats(String query, int limit) throws IOException;

    class StreamingFile {

        public final int length;
        public final InputStream is;
        final HttpConnection conn;

        public StreamingFile(int length, InputStream is, HttpConnection conn) {
            super();
            this.length = length;
            this.is = is;
            this.conn = conn;
        }

        public void close() throws IOException {
            if (is != null) is.close();
            if (conn != null) conn.close();
        }

    }

    class ClientException extends IOException {

        public ClientException(String message) {
            super(message);
        }

    }

    class InvalidCodeException extends ClientException {

        public InvalidCodeException() {
            super("Invalid code");
        }

    }

}
