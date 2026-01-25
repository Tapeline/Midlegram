package midp.tapeline.midlegram.filesystem;

import java.io.IOException;
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

    public synchronized void write(String filename, BinaryWriter writer) throws IOException {
        FS.write(fileName(filename), writer);
    }

    public synchronized Object read(String filename, BinaryReader reader) throws IOException {
        return FS.read(fileName(filename), reader);
    }

    public synchronized void delete(String filename) throws IOException {
        FS.delete(fileName(filename));
    }

    public synchronized Vector listInRoot() throws IOException {
        return FS.list(rootDir);
    }

}
