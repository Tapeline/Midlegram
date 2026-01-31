package midp.tapeline.midlegram.client;

import java.io.*;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import midp.tapeline.midlegram.StringUtils;
import midp.tapeline.midlegram.serialization.Deserializer;
import midp.tapeline.midlegram.state.data.Chat;
import midp.tapeline.midlegram.state.data.PaginatedChats;

public class MidlegramClient implements TelegramClient {

    private String sessionKey;
    private String url;
    private volatile HttpConnection pollingConnection = null;
    private volatile DataInputStream pollingInput = null;
    private volatile boolean isPollingWaiting = false;

    public MidlegramClient(String url, String sessionKey) {
        this.url = url;
        this.sessionKey = sessionKey;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    static boolean readOpSuccess(DataInputStream dis) throws IOException {
        return dis.readByte() == 0;
    }

    public void startAuth(String phone) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = (HttpConnection) Connector.open(
                url + "/api/account/login/phone?phone=" + phone,
                Connector.READ_WRITE,
                true
            );
            conn.setRequestProperty("X-Phone", phone);
            conn.setRequestMethod("POST");
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            assertOpSuccess(readOpSuccess(dis));
            sessionKey = dis.readUTF();
            System.out.println(sessionKey);
        } finally {
            if (dis != null) dis.close();
            if (conn != null) conn.close();
        }
    }

    public void confirmCode(String code) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = openSessionHttp("POST", "/api/account/login/code?code=" + code);
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            assertOpSuccess(readOpSuccess(dis));
            byte verdict = dis.readByte();
            if (verdict != 0) throw new InvalidCodeException();
        } finally {
            if (dis != null) dis.close();
            if (conn != null) conn.close();
        }
    }

    public Vector getFolders() throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = openSessionHttp("GET", "/api/folders");
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            Deserializer des = new Deserializer(dis);
            assertOpSuccess(readOpSuccess(dis));
            return des.readChatFolderList();
        } finally {
            if (dis != null) dis.close();
            if (conn != null) conn.close();
        }
    }

    public Vector getChatsIds(long folderId) throws IOException {
        return getChatsIds(folderId, 0, 1000);
    }

    public Vector getChatsIds(long folderId, int offset, int limit) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = openSessionHttp("GET", "/api/folders/" + folderId + "/chats_ids?limit=" + limit + "&offset=" + offset);
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            Deserializer des = new Deserializer(dis);
            assertOpSuccess(readOpSuccess(dis));
            return des.readIdList();
        } finally {
            if (dis != null) dis.close();
            if (conn != null) conn.close();
        }
    }

    public PaginatedChats getChats(long folderId, int page) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = openSessionHttp("GET", "/api/folders/" + folderId + "/chats?limit=1000");
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            Deserializer des = new Deserializer(dis);
            assertOpSuccess(readOpSuccess(dis));
            int totalPages = dis.readInt();
            return new PaginatedChats(des.readChatList(), totalPages);
        } finally {
            if (dis != null) dis.close();
            if (conn != null) conn.close();
        }
    }

    public Chat getChat(long chatId) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = openSessionHttp("GET", "/api/chats/" + chatId);
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            Deserializer des = new Deserializer(dis);
            assertOpSuccess(readOpSuccess(dis));
            return des.readChat();
        } finally {
            if (dis != null) dis.close();
            if (conn != null) conn.close();
        }
    }

    public Vector pollUpdates(long chatId) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = (HttpConnection) Connector.open(url + "/api/chats/" + chatId + "/updates?t=1", Connector.READ, true);
            conn.setRequestProperty("Authorization", sessionKey);
            conn.setRequestMethod("GET");
            System.out.println("Opening is");
            pollingConnection = conn;
            isPollingWaiting = true;
            dis = conn.openDataInputStream();
            System.out.println("Opened is");
            pollingInput = dis;
            System.out.println("Reading initial byte");
            dis.readByte();
            Deserializer des = new Deserializer(dis);
            System.out.println("Waiting for success");
            assertOpSuccess(readOpSuccess(dis));
            System.out.println("Reading message list");
            return des.readMessageList();
        } finally {
            pollingConnection = null;
            isPollingWaiting = false;
            pollingInput = null;
            if (dis != null) dis.close();
            if (conn != null) conn.close();
        }
    }

    public void interruptPolling() throws IOException {
        if (isPollingWaiting) {
            if (pollingConnection != null) {
                System.out.println("Closing the connection forcibly");
                pollingConnection.close();
            }
        }
    }

    public void connectClient() throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = openSessionHttp("POST", "/api/connect");
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            Deserializer des = new Deserializer(dis);
            assertOpSuccess(readOpSuccess(dis));
        } finally {
            if (dis != null) dis.close();
            if (conn != null) conn.close();
        }
    }

    public Vector getMessages(long chatId, long fromMsgId, int count) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = openSessionHttp("GET", "/api/chats/" + chatId + "/messages?from_msg=" + fromMsgId + "&lim=" + count);
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            Deserializer des = new Deserializer(dis);
            assertOpSuccess(readOpSuccess(dis));
            return des.readMessageList();
        } finally {
            if (dis != null) dis.close();
            if (conn != null) conn.close();
        }
    }

    public void sendTextMessage(long chatId, long replyTo, String message) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        String path = "/api/chats/" + chatId + "/send/text";
        if (replyTo != 0) path += "?reply=" + replyTo;
        try {
            conn = openSessionHttp("POST", path);
            byte[] bytes = message.getBytes("UTF-8");
            conn.setRequestProperty("Content-Length", "" + bytes.length);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            dos = conn.openDataOutputStream();
            dos.write(bytes);
            dos.flush();
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            Deserializer des = new Deserializer(dis);
            assertOpSuccess(readOpSuccess(dis));
        } finally {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (conn != null) conn.close();
        }
    }

    public byte[] getFile(int id, String mimetype) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = openSessionHttp("GET", "/api/file/" + id + "?mime=" + mimetype);
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            int length = conn.getHeaderFieldInt("Content-Length", 0);
            byte[] content = new byte[length];
            dis.readFully(content);
            return content;
        } finally {
            if (dis != null) dis.close();
            if (conn != null) conn.close();
        }
    }

    public StreamingFile getFileStream(int id, String mimetype) throws IOException {
        HttpConnection conn = null;
        InputStream is = null;
        try {
            conn = openSessionHttp("GET", "/api/file/" + id + "?mime=" + mimetype);
            assertRespOk(conn);
            is = conn.openInputStream();
            int length = conn.getHeaderFieldInt("Content-Length", 0);
            return new StreamingFile(length, is, conn);
        } finally {
            if (is != null) is.close();
            if (conn != null) conn.close();
        }
    }

    public String getFileToFile(int id, String mimetype) throws IOException {
        StreamingFile file = getFileStream(id, mimetype);
        String ext = "";
        if (mimetype.startsWith("audio"))
            ext = ".aac";
        else if (mimetype.startsWith("video"))
            ext = ".mp4";
        else if (mimetype.startsWith("image"))
            ext = ".jpg";
        else
            throw new IOException("Unknown mimetype " + mimetype);
        String filename = System.getProperty("fileconn.dir.private") + StringUtils.rand32() + ext;
        FileConnection fc = null;
        OutputStream os = null;
        try {
            fc = (FileConnection) Connector.open(filename, Connector.WRITE);
            os = fc.openOutputStream();
            for (int i = 0; i < fc.fileSize(); ++i)
                os.write(file.is.read());
            return filename;
        } finally {
            file.close();
            if (fc != null) fc.close();
            if (os != null) os.close();
        }
    }

    public Vector searchChats(String query, int limit) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        try {
            conn = openSessionHttp("GET", "/api/chats/search?q=" + StringUtils.urlEncode(query) + "&limit=" + limit);
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            Deserializer des = new Deserializer(dis);
            assertOpSuccess(readOpSuccess(dis));
            return des.readChatList();
        } finally {
            if (dis != null) dis.close();
            if (conn != null) conn.close();
        }
    }

    public void sendFileMessage(long chatId, long replyTo, String type, byte[] data) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        String path = "/api/chats/" + chatId + "/send/file/" + type;
        if (replyTo != 0) path += "?reply=" + replyTo;
        try {
            conn = openSessionHttp("POST", path);
            conn.setRequestProperty("Content-Length", "" + data.length);
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            dos = conn.openDataOutputStream();
            dos.write(data);
            dos.flush();
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            Deserializer des = new Deserializer(dis);
            assertOpSuccess(readOpSuccess(dis));
        } finally {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (conn != null) conn.close();
        }
    }

    public void sendFileMessage(long chatId, long replyTo, String type, String file) throws IOException {
        HttpConnection conn = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        InputStream fis = null;
        FileConnection fc = null;
        String path = "/api/chats/" + chatId + "/send/file/" + type;
        if (replyTo != 0) path += "?reply=" + replyTo;
        try {
            fc = (FileConnection) Connector.open(file, Connector.READ);
            fis = fc.openInputStream();
            conn = openSessionHttp("POST", path);
            conn.setRequestProperty("Content-Length", "" + fc.fileSize());
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            dos = conn.openDataOutputStream();
            for (long i = 0; i < fc.fileSize(); i++)
                dos.write(fis.read());
            dos.flush();
            assertRespOk(conn);
            dis = conn.openDataInputStream();
            Deserializer des = new Deserializer(dis);
            assertOpSuccess(readOpSuccess(dis));
        } finally {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (conn != null) conn.close();
            if (fis != null) fis.close();
            if (fc != null) fc.close();
        }
    }

    private HttpConnection openSessionHttp(String method, String path) throws IOException {
        HttpConnection conn = (HttpConnection) Connector.open(url + path, Connector.READ_WRITE, true);
        conn.setRequestProperty("Authorization", sessionKey);
        conn.setRequestMethod(method);
        return conn;
    }

    private void assertOpSuccess(boolean success) throws IOException {
        if (!success) throw new ClientException("Operation wasn't successful");
    }

    private void assertRespOk(HttpConnection conn) throws IOException {
        int code = conn.getResponseCode();
        if (code / 100 != 2) throw new ClientException("Unexpected response " + code);
    }

}
