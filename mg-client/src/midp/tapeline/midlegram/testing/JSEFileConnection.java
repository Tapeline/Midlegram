package midp.tapeline.midlegram.testing;

import javax.microedition.io.file.FileConnection;
import java.io.*;
import java.util.Enumeration;
import java.util.Vector;

public class JSEFileConnection implements FileConnection {

    private File file;
    private boolean isOpen;

    JSEFileConnection(String path) {
        // Strip file:// protocol if present
        if (path.startsWith("file://")) {
            path = path.substring(7);
        }
        // Handle local file system root relative paths if necessary
        this.file = new File(path);
        this.isOpen = true; // Connections are typically "open" upon creation in this context
    }

    JSEFileConnection(File file) {
        this.file = file;
        this.isOpen = true;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public InputStream openInputStream() throws IOException {
        ensureOpen();
        if (file.isDirectory()) {
            throw new IOException("Cannot open InputStream on a directory");
        }
        return new FileInputStream(file);
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    public OutputStream openOutputStream() throws IOException {
        ensureOpen();
        if (file.isDirectory()) {
            throw new IOException("Cannot open OutputStream on a directory");
        }
        // False implies overwrite, which matches standard openOutputStream behavior unless offset is used
        return new FileOutputStream(file, false);
    }

    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }

    public OutputStream openOutputStream(long offset) throws IOException {
        ensureOpen();
        if (file.isDirectory()) {
            throw new IOException("Cannot open OutputStream on a directory");
        }

        // RandomAccessFile is required to seek to an offset
        final RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.seek(offset);

        // Wrap RandomAccessFile in an OutputStream
        return new OutputStream() {
            public void write(int b) throws IOException {
                raf.write(b);
            }

            public void write(byte[] b) throws IOException {
                raf.write(b);
            }

            public void write(byte[] b, int off, int len) throws IOException {
                raf.write(b, off, len);
            }

            public void close() throws IOException {
                raf.close();
            }
        };
    }

    public long totalSize() {
        // Not supported in Java 1.1 (requires Java 1.6+)
        return -1;
    }

    public long availableSize() {
        // Not supported in Java 1.1 (requires Java 1.6+)
        return -1;
    }

    public long usedSize() {
        // Not supported in Java 1.1
        return -1;
    }

    public long directorySize(boolean includeSubDirs) throws IOException {
        ensureOpen();
        if (!file.isDirectory()) {
            throw new IOException("Not a directory");
        }
        return calculateDirectorySize(file, includeSubDirs);
    }

    private long calculateDirectorySize(File dir, boolean recurse) {
        long size = 0;
        String[] list = dir.list();
        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File child = new File(dir, list[i]);
                if (child.isDirectory()) {
                    if (recurse) {
                        size += calculateDirectorySize(child, true);
                    }
                } else {
                    size += child.length();
                }
            }
        }
        return size;
    }

    public long fileSize() throws IOException {
        ensureOpen();
        if (!file.exists()) {
            return -1;
        }
        if (file.isDirectory()) {
            return -1;
        }
        return file.length();
    }

    public boolean canRead() {
        return file.exists() && file.canRead();
    }

    public boolean canWrite() {
        return file.exists() && file.canWrite();
    }

    public boolean isHidden() {
        // Java 1.1 File API does not support isHidden().
        // Common convention: files starting with '.' are hidden.
        return file.getName().startsWith(".");
    }

    public void setReadable(boolean b) throws IOException {
        ensureOpen();
        if (!file.exists()) throw new IOException("File does not exist");
        // Not supported in Java 1.1 (File.setReadable added in 1.6)
        // If strict 1.1 is required, we can't do this.
        // We silently ignore or throw IOException depending on strictness requirements.
    }

    public void setWritable(boolean b) throws IOException {
        ensureOpen();
        if (!file.exists()) throw new IOException("File does not exist");
        // Not supported in Java 1.1 (File.setWritable added in 1.6)
    }

    public void setHidden(boolean b) throws IOException {
        ensureOpen();
        if (!file.exists()) throw new IOException("File does not exist");
        // Not supported in Java 1.1 (Native attribute access needed)
    }

    public Enumeration list() throws IOException {
        return list("*", false);
    }

    public Enumeration list(String filter, boolean includeHidden) throws IOException {
        ensureOpen();
        if (!file.isDirectory()) {
            throw new IOException("Not a directory");
        }

        String[] rawList = file.list();
        Vector v = new Vector();

        if (rawList != null) {
            for (int i = 0; i < rawList.length; i++) {
                String name = rawList[i];
                File f = new File(file, name);

                // Check hidden
                boolean isHid = name.startsWith(".");
                if (isHid && !includeHidden) {
                    continue;
                }

                // Check filter (Simple wildcard support logic)
                if (matches(name, filter)) {
                    if (f.isDirectory() && !name.endsWith("/")) {
                        v.addElement(name + "/");
                    } else {
                        v.addElement(name);
                    }
                }
            }
        }
        return v.elements();
    }

    // Simple wildcard matcher for Java 1.1 compatibility
    private boolean matches(String text, String pattern) {
        if (pattern == null || pattern.equals("*")) return true;
        // This is a very basic wildcard implementation.
        // For robust support, a full parser is needed, but this covers generic cases.
        int starIndex = pattern.indexOf('*');
        if (starIndex == -1) {
            return text.equals(pattern);
        }

        String prefix = pattern.substring(0, starIndex);
        String suffix = pattern.substring(starIndex + 1);

        return text.startsWith(prefix) && text.endsWith(suffix);
    }

    public void create() throws IOException {
        ensureOpen();
        if (file.exists()) {
            throw new IOException("File already exists");
        }
        if (file.isDirectory()) {
            throw new IOException("Path specifies a directory");
        }

        // Java 1.1 workaround for createNewFile()
        FileOutputStream fos = new FileOutputStream(file);
        fos.close();
    }

    public void mkdir() throws IOException {
        ensureOpen();
        if (file.exists()) {
            throw new IOException("Directory already exists");
        }
        if (!file.mkdir()) {
            throw new IOException("Failed to create directory");
        }
    }

    public boolean exists() {
        return file.exists();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public void delete() throws IOException {
        ensureOpen();
        if (!file.exists()) {
            throw new IOException("File does not exist");
        }
        // In JSR 75, if it's a directory, it must be empty
        if (file.isDirectory()) {
            String[] l = file.list();
            if (l != null && l.length > 0) {
                throw new IOException("Directory not empty");
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete file");
        }
    }

    public void rename(String newName) throws IOException {
        ensureOpen();
        if (newName == null) throw new NullPointerException();

        File newFile = new File(file.getParent(), newName);
        if (newFile.exists()) {
            throw new IOException("Target exists");
        }
        if (!file.renameTo(newFile)) {
            throw new IOException("Rename failed");
        }
        // Update reference
        this.file = newFile;
    }

    public void truncate(long size) throws IOException {
        ensureOpen();
        if (!file.exists()) {
            throw new IOException("File does not exist");
        }
        if (size < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }

        long currentLength = file.length();
        if (size >= currentLength) {
            return; // Nothing to do, or should we extend? JSR75 usually just truncates.
        }

        // Since RandomAccessFile.setLength() is Java 1.2+, we must copy to temp
        File tempFile = new File(file.getParent(), "tmp_" + System.currentTimeMillis());

        FileInputStream fis = null;
        FileOutputStream fos = null;

        try {
            fis = new FileInputStream(file);
            fos = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            long bytesCopied = 0;
            int read;

            while (bytesCopied < size) {
                long remaining = size - bytesCopied;
                int toRead = (int) (remaining < buffer.length ? remaining : buffer.length);

                read = fis.read(buffer, 0, toRead);
                if (read == -1) break;

                fos.write(buffer, 0, read);
                bytesCopied += read;
            }
        } finally {
            if (fis != null) fis.close();
            if (fos != null) fos.close();
        }

        // Replace original with temp
        if (!file.delete()) {
            // cleanup
            tempFile.delete();
            throw new IOException("Could not delete original file during truncate");
        }

        if (!tempFile.renameTo(file)) {
            throw new IOException("Could not rename temp file during truncate");
        }
    }

    public void setFileConnection(String path) throws IOException {
        ensureOpen();
        // If path is absolute (relative to root)
        if (path.startsWith("/") || path.indexOf(':') != -1) {
            this.file = new File(path);
        } else {
            // Relative to current
            this.file = new File(file, path);
        }
    }

    public String getName() {
        return file.getName();
    }

    public String getPath() {
        return file.getPath();
    }

    public String getURL() {
        // Simple file URL construction
        String path = file.getAbsolutePath();
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return "file://" + path;
    }

    public long lastModified() {
        return file.lastModified();
    }

    public void close() throws IOException {
        this.isOpen = false;
    }

    private void ensureOpen() throws IOException {
        if (!isOpen) {
            throw new IOException("Connection is closed");
        }
    }
}