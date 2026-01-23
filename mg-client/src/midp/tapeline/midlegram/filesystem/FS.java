package midp.tapeline.midlegram.filesystem;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

public class FS {

    public static void ensureDirExists(String path) throws IOException {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(path);
            if (!fc.exists()) fc.mkdir();
        } finally {
            if (fc != null) fc.close();
        }
    }

    public static Vector list(String path) throws IOException {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(path);
            Vector v = new Vector();
            Enumeration e = fc.list();
            while (e.hasMoreElements()) v.addElement(e.nextElement());
            return v;
        } finally {
            if (fc != null) fc.close();
        }
    }

    public static void delete(String path) throws IOException {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(path);
            fc.delete();
        } finally {
            if (fc != null) fc.close();
        }
    }

}
