package midp.tapeline.midlegram.database;

import midp.tapeline.midlegram.state.data.Message;

import java.io.IOException;
import java.util.Vector;

public class MessageDB {

    public MessageDB(String rootDir) {

    }

    public synchronized Vector getMessages(
        long chatId, long fromMessageId, int count
    ) throws IOException {
        return new Vector();
    }

    public synchronized void addMessage(Message message) throws IOException {

    }

    public synchronized void deleteMessage(long chatId, long messageId) {

    }

    public synchronized void updateMessage(Message message) throws IOException {

    }

    public synchronized Message getMessage(long chatId, long messageId) throws IOException {
        return null;
    }

}
