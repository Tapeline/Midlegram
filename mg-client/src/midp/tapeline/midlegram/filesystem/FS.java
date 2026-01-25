package midp.tapeline.midlegram.filesystem;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

public class FS {

    public static void ensureDirExists(String path) throws IOException {
        FileConnection fc = null;
        try {
            fc = open(path);
            if (!fc.exists()) fc.mkdir();
        } finally {
            if (fc != null) fc.close();
        }
    }

    public static Vector list(String path) throws IOException {
        FileConnection fc = null;
        try {
            fc = open(path);
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
            fc = open(path);
            fc.delete();
        } finally {
            if (fc != null) fc.close();
        }
    }

    public static void append(String filename, BinaryWriter writer) throws IOException {
        writeImpl(filename, writer, true, false);
    }

    public static void write(String filename, BinaryWriter writer) throws IOException {
        writeImpl(filename, writer, true, true);
    }

    public static void writeDefault(String filename, BinaryWriter writer) throws IOException {
        writeImpl(filename, writer, false, true);
    }

    public static void append(FileConnection fc, BinaryWriter writer) throws IOException {
        writeImpl(fc, writer, true, false);
    }

    public static void write(FileConnection fc, BinaryWriter writer) throws IOException {
        writeImpl(fc, writer, true, true);
    }

    public static void writeDefault(FileConnection fc, BinaryWriter writer) throws IOException {
        writeImpl(fc, writer, false, true);
    }

    private static void writeImpl(
        String filename,
        BinaryWriter writer,
        boolean writeIfExists,
        boolean overwriteContents
    ) throws IOException {
        FileConnection fc = null;
        try {
            fc = open(filename);
            writeImpl(fc, writer, writeIfExists, overwriteContents);
        } finally {
            if (fc != null) fc.close();
        }
    }

    private static void writeImpl(
        FileConnection fc,
        BinaryWriter writer,
        boolean writeIfExists,
        boolean overwriteContents
    ) throws IOException {
        OutputStream os = null;
        try {
            if (fc.exists() && !writeIfExists) return;
            if (!fc.exists() && writeIfExists) fc.create();
            if (overwriteContents) fc.truncate(0);
            os = fc.openOutputStream();
            writer.write(os);
        } finally {
            if (os != null) os.close();
        }
    }

    public static Object read(FileConnection fc, BinaryReader reader) throws IOException {
        InputStream is = null;
        try {
            if (!fc.exists()) return null;
            fc.truncate(0);
            is = fc.openInputStream();
            return reader.read(is);
        } finally {
            if (is != null) is.close();
        }
    }

    public static Object read(String filename, BinaryReader reader) throws IOException {
        FileConnection fc = null;
        try {
            fc = open(filename);
            return read(fc, reader);
        } finally {
            if (fc != null) fc.close();
        }
    }

    public static FileConnection open(String filename) throws IOException {
        FileConnection fc = (FileConnection) Connector.open(filename);
        if (fc == null) throw new IOException("Opened connection returned null");
        return fc;
    }

}
