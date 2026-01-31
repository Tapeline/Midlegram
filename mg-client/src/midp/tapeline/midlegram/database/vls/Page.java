package midp.tapeline.midlegram.database.vls;

import midp.tapeline.midlegram.filesystem.FileConnector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Page {

    private final String path;
    private FileConnection fc = null;

    public Page(String path) {
        this.path = path;
    }

    public void open() throws IOException {
        fc = (FileConnection) FileConnector.open(path, Connector.READ_WRITE);
    }

    public void close() throws IOException {
        if (fc != null) fc.close();
    }

    public void createNew() throws IOException {
        ensureOpen();
        fc.create();
    }

    private void ensureOpen() throws IOException {
        if (fc == null) throw new IOException("VLS TOC not open");
    }

    public byte[] read(RecordDescriptor record) throws IOException {
        ensureOpen();
        DataInputStream dis = null;
        try {
            dis = fc.openDataInputStream();
            dis.skipBytes((int) record.offset);
            byte[] data = new byte[(int) record.length];
            int result = dis.read(data, 0, (int) record.length);
            if (result != record.length)
                throw new IOException("Failed to read " + record + " from " + this);
            return data;
        } finally {
            if (dis != null) dis.close();
        }
    }

    public void write(RecordDescriptor record, byte[] data) throws IOException {
        ensureOpen();
        OutputStream os = null;
        try {
            os = fc.openOutputStream(record.offset);
            os.write(data);
        } finally {
            if (os != null) os.close();
        }
    }

    public void inPlaceUpdate(RecordDescriptor record, byte[] data) throws IOException {
        ensureOpen();
        byte[] tailData = read(new RecordDescriptor(
            -1,
            record.offset + record.length,
            fc.fileSize() - (record.offset + record.length),
            false
        ));
        write(record, data);
        write(new RecordDescriptor(
            -1, record.offset + data.length, tailData.length, false
        ), tailData);
    }

    public String toString() {
        return "Page{" +
            "path='" + path + '\'' +
            '}';
    }

}
