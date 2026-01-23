package midp.tapeline.midlegram.state;

import midp.tapeline.midlegram.filesystem.DataReader;
import midp.tapeline.midlegram.serialization.Deserializer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class DeserializerReader implements DataReader {

    public abstract Object readImpl(Deserializer des) throws IOException;

    public Object read(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        Deserializer des = new Deserializer(dis);
        try {
            return readImpl(des);
        } finally {
            dis.close();
        }
    }

    public static class Folders extends DeserializerReader {
        public Object readImpl(Deserializer des) throws IOException {
            return des.readChatFolderList();
        }
    }

    public static class Chats extends DeserializerReader {
        int offset = 0;
        int limit = 0;

        public Chats(int offset, int limit) {
            this.offset = offset;
            this.limit = limit;
        }

        public Object readImpl(Deserializer des) throws IOException {
            return des.readChatList();
        }
    }

    public static class Messages extends DeserializerReader {
        public Object readImpl(Deserializer des) throws IOException {
            return des.readMessageList();
        }
    }

}
