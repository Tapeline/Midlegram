package midp.tapeline.midlegram.database.fpls;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PageIndex {

    private static final int HEADER_SIZE = 4;
    private static final int RECORD_SIZE = 18;

    private String path;
    private FileConnection fc = null;
    private boolean increasing;

    public PageIndex(String path, boolean increasing) {
        this.path = path;
        this.increasing = increasing;
    }

    public void open() throws IOException {
        fc = (FileConnection) Connector.open(path);
    }

    public void close() throws IOException {
        if (fc != null) fc.close();
    }

    private void ensureOpen() throws IOException {
        if (fc == null) throw new IOException("VLS page index not open");
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

            if (increasing) {
                if (gid > recordGid)
                    low = mid + 1;
                else
                    high = mid - 1;
            } else {
                if (gid < recordGid)
                    low = mid + 1;
                else
                    high = mid - 1;
            }
        }
        return null;
    }

    public void markDeleted(RecordDescriptor descriptor) throws IOException {
        ensureOpen();
        OutputStream os = null;
        DataOutputStream dos = null;
        try {
            os = fc.openOutputStream(descriptor.offset + 8 + 8 + 8);
            dos = new DataOutputStream(os);
            dos.writeBoolean(true);
        } finally {
            if (dos != null) dos.close();
            if (os != null) os.close();
        }
    }


}
