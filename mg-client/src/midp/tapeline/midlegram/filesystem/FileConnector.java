package midp.tapeline.midlegram.filesystem;

import midp.tapeline.midlegram.testing.JSEConnector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import java.io.IOException;

public class FileConnector {

    public static boolean useJse = false;

    public static FileConnection open(String path) throws IOException {
        if (useJse)
            return JSEConnector.open(path);
        else
            return (FileConnection) Connector.open(path);
    }

    public static FileConnection open(String path, int mode) throws IOException {
        if (useJse)
            return JSEConnector.open(path, mode);
        else
            return (FileConnection) Connector.open(path, mode);
    }

    public static FileConnection open(String path, int mode, boolean timeouts) throws IOException {
        if (useJse)
            return JSEConnector.open(path, mode);
        else
            return (FileConnection) Connector.open(path, mode, timeouts);
    }

}
