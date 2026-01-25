package midp.tapeline.midlegram.state;

import midp.tapeline.midlegram.G;
import midp.tapeline.midlegram.database.MessageDB;
import midp.tapeline.midlegram.filesystem.Storage;
import midp.tapeline.midlegram.serialization.DeserializerReader;
import midp.tapeline.midlegram.serialization.SerializerWriter;
import midp.tapeline.midlegram.state.data.Message;

import java.io.IOException;
import java.util.Vector;

public class TelegramStore {

    private final Storage storage;
    private final MessageDB messageDb;
    private Vector folders = new Vector();

    public TelegramStore(Storage storage, MessageDB messageDb) {
        this.storage = storage;
        this.messageDb = messageDb;
    }

    public void init() throws IOException {
        storage.init();
        folders = (Vector) storage.read(
            "folders.dat",
            new DeserializerReader.Folders()
        );
        if (folders == null) {
            storage.write(
                "folders.dat",
                new SerializerWriter.Folders(new Vector())
            );
            folders = new Vector();
        }
    }

    public Vector getChatFolders() {
        return folders;
    }

    public void updateChatFolders(final Vector newFolders) {
        folders = newFolders;
        new Thread(new Runnable() {
            public void run() {
                try {
                    storage.write(
                        "folders.dat",
                        new SerializerWriter.Folders(newFolders)
                    );
                    invalidateChats();
                } catch (Exception e) {
                    G.logger.error("Failed to save folder data", e);
                }
            }
        }).start();
    }

    private void invalidateChats() throws IOException {
        Vector files = storage.listInRoot();
        for (int i = 0; i < files.size(); i++)
            if (((String) files.elementAt(i)).startsWith("chats_"))
                storage.delete((String) files.elementAt(i));
    }

    public synchronized Vector getChats(
        long folderId, int limit, int offset
    ) throws IOException {
        String filename = "chats_" + folderId +".dat";
        Vector chats = (Vector) storage.read(
            filename,
            new DeserializerReader.Chats(offset, limit)
        );
        if (chats == null) {
            storage.write(
                filename,
                new SerializerWriter.Chats(new Vector())
            );
            chats = new Vector();
        }
        return chats;
    }

    public void updateChats(final long folderId, final Vector newChats) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    storage.write(
                        "chats_" + folderId +".dat",
                        new SerializerWriter.Chats(newChats)
                    );
                } catch (Exception e) {
                    G.logger.error(
                        "Failed to save chats data for folder " + folderId, e
                    );
                }
            }
        }).start();
    }

    public synchronized Vector getMessages(
        long chatId, long fromMessageId, int count
    ) throws IOException {
        return messageDb.getMessages(chatId, fromMessageId, count);
    }

    public synchronized void addMessage(Message message) throws IOException {
        messageDb.addMessage(message);
    }

    public synchronized void deleteMessage(long chatId, long messageId) {
        messageDb.deleteMessage(chatId, messageId);
    }

    public synchronized void updateMessage(Message message) throws IOException {
        messageDb.updateMessage(message);
    }

    public synchronized Message getMessage(long chatId, long messageId) throws IOException {
        return messageDb.getMessage(chatId, messageId);
    }

}
