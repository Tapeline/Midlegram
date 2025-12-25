import java.io.DataInputStream;

public class Deserializer {
    DataInputStream dis;

    public Deserializer(DataInputStream dis) {
        this.dis = dis;
    }

    public java.lang.String readString() throws java.io.IOException {
        int length = dis.readShort();
        byte[] bytes = new byte[length];
        dis.readFully(bytes);
        return new java.lang.String(bytes);
    }

    public int readInt() throws java.io.IOException {
        return dis.readInt();
    }

    public long readId() throws java.io.IOException {
        return dis.readLong();
    }

    public midp.tapeline.midlegram.client.data.Chat readChat() throws java.io.IOException {
        long id = dis.readLong();
        java.lang.String title = readString();
        int unreadCount = dis.readInt();
        boolean hasLastMessage = dis.readBoolean();
        java.lang.String lastMessage = hasLastMessage ? readString() : null;
        return new midp.tapeline.midlegram.client.data.Chat(id, title, unreadCount, hasLastMessage, lastMessage);
    }

    public midp.tapeline.midlegram.client.data.ChatFolder readChatFolder() throws java.io.IOException {
        long id = dis.readLong();
        java.lang.String title = readString();
        return new midp.tapeline.midlegram.client.data.ChatFolder(id, title);
    }

    public int readMessageType() throws java.io.IOException {
        return dis.readByte();
    }

    public midp.tapeline.midlegram.client.data.Message readMessage() throws java.io.IOException {
        long id = dis.readLong();
        int type = dis.readByte();
        int timestamp = dis.readInt();
        long senderId = dis.readLong();
        java.lang.String senderName = readString();
        java.lang.String senderHandle = readString();
        java.lang.String text = readString();
        return new midp.tapeline.midlegram.client.data.Message(id, type, timestamp, senderId, senderName, senderHandle, text);
    }

    public java.util.Vector readChatFolderList() throws java.io.IOException {
        int length = dis.readInt();
        java.util.Vector chatFolders = new java.util.Vector(length);
        for (int i = 0; i < length; i++) {
            chatFolders.addElement(readChatFolder());
        }
        return chatFolders;
    }

    public java.util.Vector readChatList() throws java.io.IOException {
        int length = dis.readInt();
        java.util.Vector chats = new java.util.Vector(length);
        for (int i = 0; i < length; i++) {
            chats.addElement(readChat());
        }
        return chats;
    }

    public java.util.Vector readMessageList() throws java.io.IOException {
        int length = dis.readInt();
        java.util.Vector messages = new java.util.Vector(length);
        for (int i = 0; i < length; i++) {
            messages.addElement(readMessage());
        }
        return messages;
    }
}
