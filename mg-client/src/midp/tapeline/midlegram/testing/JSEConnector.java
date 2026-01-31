package midp.tapeline.midlegram.testing;

import javax.microedition.io.file.FileConnection;

public class JSEConnector {

    public static FileConnection open(String path) {
        return new JSEFileConnection(path);
    }

    public static FileConnection open(String path, int mode) {
        return new JSEFileConnection(path);
    }

}
