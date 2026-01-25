package midp.tapeline.midlegram.database.vls;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TableOfContents {

    private static final int HEADER_SIZE = 12;
    private static final int DESCRIPTOR_SIZE = 17;

    private final String path;
    private FileConnection fc = null;

    public TableOfContents(String path) {
        this.path = path;
    }

    public void open() throws IOException {
        fc = (FileConnection) Connector.open(path, Connector.READ_WRITE);
    }

    public void close() throws IOException {
        if (fc != null) fc.close();
    }

    private void ensureOpen() throws IOException {
        if (fc == null) throw new IOException("VLS TOC not open");
    }

    public void ensureExists(long maxPageSize) throws IOException {
        if (!fc.exists()) {
            fc.create();
            writePageCount(0);
            writeMaxPageSize(maxPageSize);
        }
    }

    public int readPageCount() throws IOException {
        ensureOpen();
        DataInputStream dis = null;
        try {
            dis = fc.openDataInputStream();
            return dis.readInt();
        } finally {
            if (dis != null) dis.close();
        }
    }

    public void writePageCount(int pageCount) throws IOException {
        ensureOpen();
        DataOutputStream dos = null;
        try {
            dos = fc.openDataOutputStream();
            dos.writeInt(pageCount);
        } finally {
            if (dos != null) dos.close();
        }
    }

    public long readMaxPageSize() throws IOException {
        ensureOpen();
        DataInputStream dis = null;
        try {
            dis = fc.openDataInputStream();
            dis.skipBytes(4); // skip page_count
            return dis.readLong();
        } finally {
            if (dis != null) dis.close();
        }
    }

    public void writeMaxPageSize(long maxPageSize) throws IOException {
        ensureOpen();
        OutputStream os = null;
        DataOutputStream dos = null;
        try {
            os = fc.openOutputStream(4);
            dos = new DataOutputStream(os);
            dos.writeLong(maxPageSize);
        } finally {
            if (dos != null) dos.close();
            if (os != null) os.close();
        }
    }

    public PageDescriptor maybeGetPageContainingGid(long gid) throws IOException {
        ensureOpen();
        int low = 0;
        int high = readPageCount();
        long pageStart;
        long pageEnd;
        boolean pageAvailable;
        while (low <= high) {
            int mid = low + (high - low) / 2;

            DataInputStream dis = null;
            try {
                dis = fc.openDataInputStream();
                dis.skipBytes(HEADER_SIZE + mid * DESCRIPTOR_SIZE);
                pageStart = dis.readLong();
                pageEnd = dis.readLong();
                pageAvailable = dis.readBoolean();
            } finally {
                if (dis != null) dis.close();
            }

            if (pageStart <= gid && gid <= pageEnd)
                return new PageDescriptor(mid, pageStart, pageEnd, pageAvailable);

            if (gid > pageEnd)
                low = mid + 1;
            else
                high = mid - 1;
        }
        return null;
    }

    public void updatePageDescriptor(PageDescriptor descriptor) throws IOException {
        ensureOpen();
        OutputStream os = null;
        DataOutputStream dos = null;
        try {
            os = fc.openOutputStream(HEADER_SIZE + (long) DESCRIPTOR_SIZE * descriptor.id);
            dos = new DataOutputStream(os);
            dos.writeLong(descriptor.start);
            dos.writeLong(descriptor.end);
            dos.writeBoolean(descriptor.isAvailable);
        } finally {
            if (dos != null) dos.close();
            if (os != null) os.close();
        }
    }

    public long readSmallestGid() throws IOException {
        ensureOpen();
        DataInputStream dis = null;
        try {
            dis = fc.openDataInputStream();
            int pageCount = dis.readInt();
            dis.skipBytes(8);
            // Read the first long in tuple
            return dis.readLong();
        } finally {
            if (dis != null) dis.close();
        }
    }

    public long readLargestGid() throws IOException {
        ensureOpen();
        DataInputStream dis = null;
        try {
            dis = fc.openDataInputStream();
            int pageCount = dis.readInt();
            dis.skipBytes(8);
            dis.skipBytes((pageCount - 1) * DESCRIPTOR_SIZE);
            // Read the second long in tuple
            dis.skipBytes(8);
            return dis.readLong();
        } finally {
            if (dis != null) dis.close();
        }
    }

    public PageDescriptor maybeReadLastPageDescriptor() throws IOException {
        ensureOpen();
        DataInputStream dis = null;
        long pageStart;
        long pageEnd;
        boolean pageAvailable;
        try {
            dis = fc.openDataInputStream();
            int pageCount = dis.readInt();
            if (pageCount == 0) return null;
            dis.skipBytes(8 + (pageCount - 1) * DESCRIPTOR_SIZE);
            pageStart = dis.readLong();
            pageEnd = dis.readLong();
            pageAvailable = dis.readBoolean();
            return new PageDescriptor(
                pageCount - 1, pageStart, pageEnd, pageAvailable
            );
        } finally {
            if (dis != null) dis.close();
        }
    }

    public PageDescriptor appendPageDescriptor(PageDescriptor descriptor) throws IOException {
        ensureOpen();
        int newPageId = readPageCount();
        writePageCount(newPageId + 1);
        OutputStream os = null;
        DataOutputStream dos = null;
        try {
            os = fc.openOutputStream(HEADER_SIZE + (long) DESCRIPTOR_SIZE * newPageId);
            dos = new DataOutputStream(os);
            dos.writeLong(descriptor.start);
            dos.writeLong(descriptor.end);
            dos.writeBoolean(descriptor.isAvailable);
            return new PageDescriptor(
                newPageId, descriptor.start, descriptor.end, descriptor.isAvailable
            );
        } catch (IOException e) {
            writePageCount(newPageId); // rollback
            throw e;
        } finally {
            if (dos != null) dos.close();
            if (os != null) os.close();
        }
    }

}
