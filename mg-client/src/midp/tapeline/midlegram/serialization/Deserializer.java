package midp.tapeline.midlegram.serialization;

import midp.tapeline.midlegram.state.data.Chat;
import midp.tapeline.midlegram.state.data.ChatFolder;
import midp.tapeline.midlegram.state.data.Media;
import midp.tapeline.midlegram.state.data.Message;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

public class Deserializer {

    private final DataInputStream dis;

    public Deserializer(DataInputStream dis) {
        this.dis = dis;
    }

    public ChatFolder readChatFolder() throws IOException {
        return new ChatFolder(
            dis.readLong(),
            dis.readUTF(),
            readIdList()
        );
    }

    public Chat readChat() throws IOException {
        return new Chat(
            dis.readLong(),
            dis.readUTF(),
            dis.readInt(),
            dis.readUTF()
        );
    }

    public Message readMessage() throws IOException {
        long id = dis.readLong();
        byte type = dis.readByte();
        int time = dis.readInt();
        long authorId = dis.readLong();
        String authorName = dis.readUTF();
        String authorHandle = dis.readUTF();
        String text = dis.readUTF();
        Vector media = readMediaList();
        boolean hasReply = dis.readBoolean();
        Long inReplyTo = null;
        if (hasReply)
            inReplyTo = new Long(dis.readLong());
        return new Message(
            id,
            type,
            time,
            authorId,
            authorName,
            authorHandle,
            text,
            media,
            inReplyTo
        );
    }

    public Media readMedia() throws IOException {
        return new Media(
            dis.readUTF(),
            dis.readInt(),
            dis.readLong()
        );
    }

    public Vector readIdList() throws IOException {
        int len = dis.readInt();
        Vector v = new Vector();
        for (int i = 0; i < len; ++i)
            v.addElement(new Long(dis.readLong()));
        return v;
    }

    public Vector readChatFolderList() throws IOException {
        int len = dis.readInt();
        Vector v = new Vector();
        for (int i = 0; i < len; ++i)
            v.addElement(readChatFolder());
        return v;
    }

    public Vector readChatList() throws IOException {
        int len = dis.readInt();
        Vector v = new Vector();
        for (int i = 0; i < len; ++i)
            v.addElement(readChat());
        return v;
    }

    public Vector readMediaList() throws IOException {
        int len = dis.readInt();
        Vector v = new Vector();
        for (int i = 0; i < len; ++i)
            v.addElement(readMedia());
        return v;
    }

    public Vector readMessageList() throws IOException {
        int len = dis.readInt();
        Vector v = new Vector();
        for (int i = 0; i < len; ++i)
            v.addElement(readMessage());
        return v;
    }

}
