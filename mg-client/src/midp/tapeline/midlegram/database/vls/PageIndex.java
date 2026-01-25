package midp.tapeline.midlegram.database.vls;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PageIndex {

    private static final int HEADER_SIZE = 4;
    private static final int RECORD_SIZE = 18;

    private final String path;
    private FileConnection fc = null;

    public PageIndex(String path) {
        this.path = path;
    }

    public void open() throws IOException {
        fc = (FileConnection) Connector.open(path, Connector.READ_WRITE);
    }

    public void close() throws IOException {
        if (fc != null) fc.close();
    }

    private void ensureOpen() throws IOException {
        if (fc == null) throw new IOException("VLS page index not open");
    }

    public void createNew() throws IOException {
        ensureOpen();
        fc.create();
        writeRecordCount(0);
    }

    public int readRecordCount() throws IOException {
        ensureOpen();
        DataInputStream dis = null;
        try {
            dis = fc.openDataInputStream();
            return dis.readInt();
        } finally {
            if (dis != null) dis.close();
        }
    }

    public void writeRecordCount(int pageCount) throws IOException {
        ensureOpen();
        DataOutputStream dos = null;
        try {
            dos = fc.openDataOutputStream();
            dos.writeInt(pageCount);
        } finally {
            if (dos != null) dos.close();
        }
    }

    public RecordDescriptor maybeGetRecordByGid(long gid) throws IOException {
        ensureOpen();
        int low = 0;
        int high = readRecordCount();
        long recordGid;
        long recordOffset;
        long recordLength;
        boolean isDeleted;
        while (low <= high) {
            int mid = low + (high - low) / 2;

            DataInputStream dis = null;
            try {
                dis = fc.openDataInputStream();
                dis.skipBytes(HEADER_SIZE + mid * RECORD_SIZE);
                recordGid = dis.readLong();
                recordOffset = dis.readLong();
                recordLength = dis.readLong();
                isDeleted = dis.readBoolean();
            } finally {
                if (dis != null) dis.close();
            }

            if (gid == recordGid)
                return new RecordDescriptor(recordGid, recordOffset, recordLength, isDeleted);

            if (gid > recordGid)
                low = mid + 1;
            else
                high = mid - 1;
        }
        return null;
    }

    public void updateRecordDescriptor(RecordDescriptor descriptor) throws IOException {
        ensureOpen();
        OutputStream os = null;
        DataOutputStream dos = null;
        try {
            os = fc.openOutputStream(descriptor.offset);
            dos = new DataOutputStream(os);
            dos.writeLong(descriptor.gid);
            dos.writeLong(descriptor.offset);
            dos.writeLong(descriptor.length);
            dos.writeBoolean(descriptor.isDeleted);
        } finally {
            if (dos != null) dos.close();
            if (os != null) os.close();
        }
    }

    public RecordDescriptor appendRecordDescriptor(RecordDescriptor descriptor) throws IOException {
        ensureOpen();
        int oldRecordCount = readRecordCount();
        writeRecordCount(oldRecordCount + 1);
        OutputStream os = null;
        DataOutputStream dos = null;
        DataInputStream dis = null;
        long lastId;
        try {
            if (oldRecordCount > 0) {
                dis = fc.openDataInputStream();
                dis.skipBytes((int) (HEADER_SIZE + (long) RECORD_SIZE * (oldRecordCount - 1)));
                lastId = dis.readLong();
                dis.close();
            } else lastId = -1;
            os = fc.openOutputStream(HEADER_SIZE + (long) RECORD_SIZE * oldRecordCount);
            dos = new DataOutputStream(os);
            dos.writeLong(lastId + 1);
            dos.writeLong(descriptor.offset);
            dos.writeLong(descriptor.length);
            dos.writeBoolean(descriptor.isDeleted);
            return new RecordDescriptor(
                lastId + 1, descriptor.offset,
                descriptor.length, descriptor.isDeleted
            );
        } catch (IOException e) {
            writeRecordCount(oldRecordCount); // rollback
            throw e;
        } finally {
            if (dis != null) dis.close();
            if (dos != null) dos.close();
            if (os != null) os.close();
        }
    }

}
