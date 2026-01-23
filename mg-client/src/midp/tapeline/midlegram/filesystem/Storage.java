package midp.tapeline.midlegram.filesystem;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class Storage {

    private final String rootDir;

    public Storage(String rootDir) {
        this.rootDir = rootDir;
    }

    public void init() throws IOException {
        FS.ensureDirExists(rootDir);
    }

    String fileName(String name) {
        return rootDir + name;
    }

    public synchronized void write(String filename, DataWriter writer) throws IOException {
        FileConnection fc = null;
        OutputStream os = null;
        try {
            fc = (FileConnection) Connector.open(fileName(filename));
            if (!fc.exists()) fc.create();
            fc.truncate(0);
            os = fc.openOutputStream();
            writer.write(os);
        } finally {
            if (os != null) os.close();
            if (fc != null) fc.close();
        }
    }

    public synchronized Object read(String filename, DataReader reader) throws IOException {
        FileConnection fc = null;
        InputStream is = null;
        try {
            fc = (FileConnection) Connector.open(fileName(filename));
            if (!fc.exists()) return null;
            fc.truncate(0);
            is = fc.openInputStream();
            return reader.read(is);
        } finally {
            if (is != null) is.close();
            if (fc != null) fc.close();
        }
    }

    public synchronized void delete(String filename) throws IOException {
        FS.delete(fileName(filename));
    }

    public synchronized Vector listInRoot() throws IOException {
        return FS.list(rootDir);
    }

}
