package midp.tapeline.midlegram.state;

import midp.tapeline.midlegram.filesystem.DataReader;
import midp.tapeline.midlegram.filesystem.DataWriter;
import midp.tapeline.midlegram.serialization.Deserializer;
import midp.tapeline.midlegram.serialization.Serializer;

import java.io.*;
import java.util.Vector;

public abstract class SerializerWriter implements DataWriter {

    public abstract void writeImpl(Serializer ser) throws IOException;

    public void write(OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        Serializer ser = new Serializer(dos);
        try {
            writeImpl(ser);
        } finally {
            dos.close();
        }
    }

    public static class Folders extends SerializerWriter {

        Vector v;

        public Folders(Vector v) {
            this.v = v;
        }

        public void writeImpl(Serializer ser) throws IOException {
            ser.writeChatFolderList(v);
        }

    }

    public static class Chats extends SerializerWriter {

        Vector v;

        public Chats(Vector v) {
            this.v = v;
        }

        public void writeImpl(Serializer ser) throws IOException {
            ser.writeChatList(v);
        }

    }

    public static class Messages extends SerializerWriter {

        Vector v;

        public Messages(Vector v) {
            this.v = v;
        }

        public void writeImpl(Serializer ser) throws IOException {
            ser.writeMessageList(v);
        }

    }

}
