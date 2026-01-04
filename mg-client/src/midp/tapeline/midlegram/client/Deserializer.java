package midp.tapeline.midlegram.client;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import midp.tapeline.midlegram.client.data.Chat;
import midp.tapeline.midlegram.client.data.ChatFolder;
import midp.tapeline.midlegram.client.data.Media;
import midp.tapeline.midlegram.client.data.Message;

public class Deserializer {

    private DataInputStream dis;

    public Deserializer(DataInputStream dis) {
        this.dis = dis;
    }

    public boolean readOperationSuccess() throws IOException {
        byte status = dis.readByte();
        System.out.println("Status: " + status);
        return status == 0;
    }

    public String readString() throws IOException {
        return dis.readUTF();
    }

    public Vector readFolderList() throws IOException {
        int len = dis.readInt();
        Vector folders = new Vector();
        for (int i = 0; i < len; ++i) {
            long id = dis.readLong();
            System.out.println("id " + id);
            String title = readString();
            folders.addElement(new ChatFolder(id, title));
        }
        return folders;
    }

    public Vector readIdList() throws IOException {
        int len = dis.readInt();
        System.out.println("len " + len);
        Vector chats = new Vector();
        for (int i = 0; i < len; ++i) {
            chats.addElement(new Long(dis.readLong()));
        }
        return chats;
    }

    public Vector readChatList() throws IOException {
        int len = dis.readInt();
        System.out.println("len " + len);
        Vector chats = new Vector();
        for (int i = 0; i < len; ++i) {
            chats.addElement(readChat());
        }
        return chats;
    }

    public Chat readChat() throws IOException {
        long id = dis.readLong();
        System.out.println("id " + id);
        String title = readString();
        System.out.println("title " + title + " len " + title.length());
        int unread = dis.readInt();
        System.out.println("unread " + unread);
        boolean hasLast = dis.readBoolean();
        System.out.println("hasLast " + hasLast);
        String last = null;
        if (hasLast)
            last = readString();
        return new Chat(id, title, unread, last);
    }

    public Vector readMessageListNoSenders() throws IOException {
        int len = dis.readInt();
        System.out.println("len " + len);
        Vector msgs = new Vector();
        for (int i = 0; i < len; ++i) {
            long id = dis.readLong();
            System.out.println("id " + id);
            byte type = dis.readByte();
            System.out.println("typ " + type);
            int time = dis.readInt();
            System.out.println("time " + time);
            long senderId = dis.readLong();
            System.out.println("sid " + senderId);
            String text = readString();
            System.out.println("text " + text + " len " + text.length());
            msgs.addElement(new Message(id, type, time, senderId, text));
        }
        return msgs;
    }

    public Vector readMessageList() throws IOException {
        int len = dis.readInt();
        System.out.println("len " + len);
        Vector msgs = new Vector();
        for (int i = 0; i < len; ++i) {
            long id = dis.readLong();
            System.out.println("id " + id);
            byte type = dis.readByte();
            System.out.println("typ " + type);
            int time = dis.readInt();
            System.out.println("time " + time);
            long senderId = dis.readLong();
            System.out.println("sid " + senderId);
            String authorName = readString();
            String authorHandle = readString();
            String text = readString();
            System.out.println("text " + text + " len " + text.length());
            int mediaLen = dis.readInt();
            System.out.println("medialen " + len);
            Vector media = new Vector();
            for (int j = 0; j < mediaLen; ++j) media.addElement(readMedia());
            boolean hasReply = dis.readBoolean();
            Long inReplyTo = null;
            if (hasReply)
                inReplyTo = new Long(dis.readLong());
            msgs.addElement(new Message(id, type, time, senderId, text, authorName, authorHandle, media, inReplyTo));
        }
        return msgs;
    }

    public Media readMedia() throws IOException {
        String mimetype = readString();
        int id = dis.readInt();
        long size = dis.readLong();
        return new Media(mimetype, id, size);
    }

}
