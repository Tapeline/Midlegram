package midp.tapeline.midlegram.client;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import midp.tapeline.midlegram.client.MGClient.StreamingFile;

public class Telegram {

    private static int MESSAGE_BATCH = 5;

    MGClient client;
    Hashtable chats;
    Vector folders;
    Hashtable foldersToChatIds;
    Hashtable messages;

    public Telegram(MGClient client) {
        this.client = client;
        this.chats = null;
        this.folders = null;
        this.foldersToChatIds = null;
        this.messages = new Hashtable();
    }

    public void connect() throws IOException {
        client.connectClient();
    }

    public void startAuth(String phone) throws IOException {
        client.startAuth(phone);
    }

    public void confirmCode(String code) throws IOException {
        client.confirmCode(code);
    }

    public Vector getFolders() throws IOException {
        return client.getFolders();
    }

    public ChatListPage getChats(long folderId, int offset, int limit) throws IOException {
        if (folders == null ||
                foldersToChatIds == null ||
                !foldersToChatIds.containsKey(new Long(folderId)))
            getFolders();
        Vector ids = (Vector) client.getChatsIds(folderId, offset, limit + 1);
        System.out.println("Folder " + folderId + " chats " + ids);
        Vector folderChats = new Vector();
        for (int i = 0; i < Math.min(limit, ids.size()); ++i)
            folderChats.addElement(client.getChat(((Long) ids.elementAt(i)).longValue()));
        return new ChatListPage(ids.size() > limit, folderChats);
    }

    public Vector getMessages(Long chatId, long fromMsg) throws IOException {
        return client.getMessages(chatId.longValue(), fromMsg, MESSAGE_BATCH);
    }

    public void sendTextMessage(long chatId, long replyTo, String message) throws IOException {
        client.sendTextMessage(chatId, replyTo, message);
    }

    public void sendFileMessage(long chatId, long replyTo, String type, byte[] data) throws IOException {
        client.sendFileMessage(chatId, replyTo, type, data);
    }

    public void sendFileMessage(long chatId, long replyTo, String type, String file) throws IOException {
        client.sendFileMessage(chatId, replyTo, type, file);
    }

    public byte[] getFile(int id, String mimetype) throws IOException {
        return client.getFile(id, mimetype);
    }

    public StreamingFile getFileStream(int id, String mimetype) throws IOException {
        return client.getFileStream(id, mimetype);
    }

    public String getSessionKey() {
        return client.getSessionKey();
    }

    public Vector searchChats(String query, int limit) throws IOException {
        return client.searchChats(query, limit);
    }

    public Vector pollUpdates(long chatId) throws IOException {
        return client.pollUpdates(chatId);
    }

    public void interruptPolling() throws IOException {
        client.interruptPolling();
    }

    public static class ChatListPage {
        public final boolean hasNext;
        public final Vector chats;

        public ChatListPage(boolean hasNext, Vector chats) {
            super();
            this.hasNext = hasNext;
            this.chats = chats;
        }
    }

}
