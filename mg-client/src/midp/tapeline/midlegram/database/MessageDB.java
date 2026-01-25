package midp.tapeline.midlegram.database;

import midp.tapeline.midlegram.filesystem.DataReader;
import midp.tapeline.midlegram.filesystem.DataWriter;
import midp.tapeline.midlegram.filesystem.FS;
import midp.tapeline.midlegram.serialization.Serializer;
import midp.tapeline.midlegram.state.data.Message;

import javax.microedition.io.file.FileConnection;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.DataOutputStream;
import java.util.Vector;

public class MessageDB {

    private static final long PIVOT_NOT_SET = -1;
    private static final long NO_ID = -1;
    private static final byte FORWARD_FILE = 1;
    private static final byte BACKWARD_FILE = -1;
    private static final long IDX_RECORD_SIZE = 20;

    private final String rootDir;

    private long pivot = PIVOT_NOT_SET;

    public MessageDB(String rootDir) {
        this.rootDir = rootDir;
    }

    private String fileName(String filename) {
        return rootDir + filename;
    }

    public void init() throws IOException {
        FS.ensureDirExists(rootDir);
        FS.writeDefault(
            fileName("idx_fwd.dat"), new DataWriter() {
                protected void writeData(DataOutputStream dos) throws IOException {

                }
            }
        );
        FS.writeDefault(
            fileName("fwd.dat"), new DataWriter() {
                protected void writeData(DataOutputStream os) throws IOException {

                }
            }
        );
        FS.writeDefault(
            fileName("idx_bck.dat"), new DataWriter() {
                protected void writeData(DataOutputStream os) throws IOException {

                }
            }
        );
        FS.writeDefault(
            fileName("bck.dat"), new DataWriter() {
                protected void writeData(DataOutputStream os) throws IOException {

                }
            }
        );
        FS.writeDefault(
            fileName("piv.dat"), new DataWriter() {
                protected void writeData(DataOutputStream os) throws IOException {
                    os.writeLong(PIVOT_NOT_SET);
                }
            }
        );
        FS.read(
            fileName("piv.dat"), new DataReader() {
                protected Object readData(DataInputStream dis) throws IOException {
                    pivot = dis.readLong();
                    return null;
                }
            }
        );
    }

    private byte[] serializeMessage(Message message) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        Serializer ser = new Serializer(dos);
        ser.writeMessage(message);
        dos.close();
        baos.close();
        return baos.toByteArray();
    }

    private void appendMessage(final Message message) throws IOException {
        if (message.id <= pivot)
            throw new IllegalStateException("Tried to append a message that is left to pivot");
        long lastId = getLatestIdInPart(FORWARD_FILE);
        if (lastId >= message.id)
            throw new IllegalStateException(
                "Tried to mess the order in fwd: [..." + lastId + "] cons " + message.id
            );
        addToPartBackImpl(FORWARD_FILE, message);
    }

    private void prependMessage(final Message message) throws IOException {
        if (message.id >= pivot)
            throw new IllegalStateException("Tried to append a message that is right to pivot");
        long lastId = getLatestIdInPart(BACKWARD_FILE);
        if (lastId <= message.id)
            throw new IllegalStateException(
                "Tried to mess the order in bck: [..." + lastId + "] cons " + message.id
            );
        addToPartBackImpl(BACKWARD_FILE, message);
    }

    private void addToPartBackImpl(byte part, final Message message) throws IOException {
        FileConnection fwdFile = FS.open(fileName(
            part == FORWARD_FILE? "fwd.dat" : "bck.dat"
        ));
        final long offset = fwdFile.fileSize();
        final byte[] messageBytes = serializeMessage(message);
        FS.append(
            fwdFile, new DataWriter() {
                protected void writeData(DataOutputStream dos) throws IOException {
                    dos.write(messageBytes);
                }
            }
        );
        FS.append(
            fileName(
                part == FORWARD_FILE? "idx_fwd.dat" : "idx_bck.dat"
            ), new DataWriter() {
                protected void writeData(DataOutputStream dos) throws IOException {
                    dos.writeLong(message.id);
                    dos.writeLong(offset);
                    dos.writeInt(messageBytes.length);
                }
            }
        );
    }

    private long getLatestIdInPart(byte part) throws IOException {
        FileConnection fc = FS.open(fileName(
            part == FORWARD_FILE? "idx_fwd.dat" : "idx_bck.dat"
        ));
        DataInputStream dis = null;
        try {
            long size = fc.fileSize();
            if (size == 0) return NO_ID;
            dis = fc.openDataInputStream();
            dis.skipBytes((int) ((size / IDX_RECORD_SIZE - 1) * IDX_RECORD_SIZE));
            return dis.readLong();
        } finally {
            if (dis != null) dis.close();
            fc.close();
        }
    }

    public synchronized Vector getMessages(
        long chatId, long fromMessageId, int count
    ) throws IOException {
        return new Vector();
    }

    public synchronized void addMessage(Message message) throws IOException {
        if (pivot == PIVOT_NOT_SET) {
            appendMessage(message);
            pivot = message.id;
            FS.write(
                fileName("piv.dat"), new DataWriter() {
                    protected void writeData(DataOutputStream os) throws IOException {
                        os.writeLong(pivot);
                    }
                }
            );
        } else {
            if (message.id > pivot) appendMessage(message);
            else prependMessage(message);
        }
    }

    public synchronized void deleteMessage(long chatId, long messageId) {

    }

    public synchronized void updateMessage(Message message) throws IOException {

    }

    public synchronized Message getMessage(long chatId, long messageId) throws IOException {
        return null;
    }

}
