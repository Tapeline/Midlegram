package midp.tapeline.midlegram.serialization;

import midp.tapeline.midlegram.state.data.Chat;
import midp.tapeline.midlegram.state.data.ChatFolder;
import midp.tapeline.midlegram.state.data.Media;
import midp.tapeline.midlegram.state.data.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

public class Serializer {

    private final DataOutputStream dos;

    public Serializer(DataOutputStream dos) {
        this.dos = dos;
    }

    public void writeChatFolder(ChatFolder folder) throws IOException {
        dos.writeLong(folder.id);
        dos.writeUTF(folder.name);
        writeIdList(folder.chatIds);
    }

    public void writeChat(Chat chat) throws IOException {
        dos.writeLong(chat.id);
        dos.writeUTF(chat.title);
        dos.writeInt(chat.unreadCount);
        dos.writeUTF(chat.lastMessage);
    }

    public void writeMessage(Message message) throws IOException {
        dos.writeLong(message.id);
        dos.writeByte(message.type);
        dos.writeInt(message.time);
        dos.writeLong(message.authorId);
        dos.writeUTF(message.authorName);
        dos.writeUTF(message.authorHandle);
        dos.writeUTF(message.text);
        dos.writeInt(message.media.size());
        for (int i = 0; i < message.media.size(); i++)
            writeMedia((Media) message.media.elementAt(i));
        dos.writeBoolean(message.isReply());
        if (message.isReply())
            dos.writeLong(message.getInReplyTo());
    }

    public void writeMedia(Media media) throws IOException {
        dos.writeUTF(media.mimetype);
        dos.writeInt(media.id);
        dos.writeLong(media.size);
    }

    public void writeChatFolderList(Vector v) throws IOException {
        dos.writeInt(v.size());
        for (int i = 0; i < v.size(); ++i)
            writeChatFolder((ChatFolder) v.elementAt(i));
    }

    public void writeIdList(Vector v) throws IOException {
        dos.writeInt(v.size());
        for (int i = 0; i < v.size(); ++i)
            dos.writeLong(((Long) v.elementAt(i)).longValue());
    }

    public void writeMediaList(Vector v) throws IOException {
        dos.writeInt(v.size());
        for (int i = 0; i < v.size(); ++i)
            writeMedia((Media) v.elementAt(i));
    }

    public void writeChatList(Vector v) throws IOException {
        dos.writeInt(v.size());
        for (int i = 0; i < v.size(); ++i)
            writeChat((Chat) v.elementAt(i));
    }

    public void writeMessageList(Vector v) throws IOException {
        dos.writeInt(v.size());
        for (int i = 0; i < v.size(); ++i)
            writeMessage((Message) v.elementAt(i));
    }

}
