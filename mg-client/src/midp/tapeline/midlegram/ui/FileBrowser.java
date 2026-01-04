package midp.tapeline.midlegram.ui;

import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;

public class FileBrowser extends List implements CommandListener, Runnable {

    public static interface FileSelectListener {
        void onFileSelected(String url, String filename);

        void onBrowserCancelled();
    }

    // FIX 1: ROOT must be empty so we don't prepend a slash to "E:/"
    private static final String ROOT = "";
    private String currentPath = ROOT;

    private Vector fileList = new Vector();
    private Vector typeList = new Vector();

    private Command cmdSelect;
    private Command cmdBack;
    private Command cmdCancel;

    private FileSelectListener listener;
    private Display display;

    private static final String[] VALID_EXTENSIONS = {
            ".jpg", ".jpeg", ".png", ".gif",
    };

    // Use null for default system icons to save RAM
    private static final Image ICON_DIR = null;
    private static final Image ICON_FILE = null;

    public FileBrowser(Display display, FileSelectListener listener) {
        super("Select Drive", List.IMPLICIT);
        this.display = display;
        this.listener = listener;

        cmdSelect = new Command("Select", Command.ITEM, 1);
        cmdBack = new Command("Back", Command.BACK, 1);
        cmdCancel = new Command("Cancel", Command.EXIT, 1);

        addCommand(cmdSelect);
        addCommand(cmdBack);
        setCommandListener(this);
        setSelectCommand(cmdSelect);

        // FIX 2: Do not run showRoots() in constructor.
        // It triggers a security prompt which might block the UI thread.
        // We defer it until the list is actually shown.
    }

    // Call this immediately after display.setCurrent(browser)
    public void init() {
        new Thread(new Runnable() {
            public void run() {
                showRoots();
            }
        }).start();
    }

    private void showRoots() {
        currentPath = ROOT;

        // UI updates must be serial
        display.callSerially(new Runnable() {
            public void run() {
                deleteAll();
                fileList.removeAllElements();
                typeList.removeAllElements();
                setTitle("Select Drive");
            }
        });

        // This line triggers the "Allow Read User Data?" prompt on Symbian
        Enumeration drives = FileSystemRegistry.listRoots();

        final Vector roots = new Vector();
        while (drives.hasMoreElements()) {
            roots.addElement(drives.nextElement());
        }

        display.callSerially(new Runnable() {
            public void run() {
                for (int i = 0; i < roots.size(); i++) {
                    String root = (String) roots.elementAt(i);
                    // Symbian returns "E:/", "C:/"
                    append(root, ICON_DIR);
                    fileList.addElement(root);
                    typeList.addElement(new Integer(0));
                }
            }
        });
    }

    private void openDirectory(String url) {
        currentPath = url;
        // Run in thread to avoid freezing UI during IO
        new Thread(this).start();
    }

    public void run() {
        FileConnection fc = null;
        try {
            // FIX 3: Robust URL construction
            // If currentPath is "E:/", result is "file:///E:/" (Correct)
            String connUrl = "file:///" + currentPath;

            fc = (FileConnection) Connector.open(connUrl, Connector.READ);
            Enumeration list = fc.list();

            final Vector safeFiles = new Vector();
            final Vector safeTypes = new Vector();

            while (list.hasMoreElements()) {
                String filename = (String) list.nextElement();
                boolean isDir = filename.endsWith("/");

                if (isDir) {
                    safeFiles.addElement(filename);
                    safeTypes.addElement(new Integer(0));
                } else {
                    if (isValidMedia(filename)) {
                        safeFiles.addElement(filename);
                        safeTypes.addElement(new Integer(1));
                    }
                }
            }

            final String pathTitle = currentPath;
            display.callSerially(new Runnable() {
                public void run() {
                    setTitle(pathTitle);
                    deleteAll();
                    fileList.removeAllElements();
                    typeList.removeAllElements();

                    if (safeFiles.size() == 0) {
                        append("(Empty)", null); // Visual feedback
                    }

                    for (int i = 0; i < safeFiles.size(); i++) {
                        String f = (String) safeFiles.elementAt(i);
                        Integer t = (Integer) safeTypes.elementAt(i);
                        String label = (t.intValue() == 0) ? "[" + f + "]" : f;
                        append(label, null);

                        fileList.addElement(f);
                        typeList.addElement(t);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            // On Access Denied or Error, go back
            // Run on UI thread to be safe
            display.callSerially(new Runnable() {
                public void run() {
                    UI.alertFatal("Access Denied");
                    goUp();
                }
            });
        } finally {
            try {
                if (fc != null) fc.close();
            } catch (IOException ignored) {
            }
        }
    }

    private boolean isValidMedia(String name) {
        String lower = name.toLowerCase();
        for (int i = 0; i < VALID_EXTENSIONS.length; i++) {
            if (lower.endsWith(VALID_EXTENSIONS[i])) return true;
        }
        return false;
    }

    private void goUp() {
        if (currentPath.equals(ROOT)) {
            listener.onBrowserCancelled();
            return;
        }

        // "E:/Images/" -> "E:/"
        // "E:/" -> ROOT

        int lastSlash = currentPath.lastIndexOf('/', currentPath.length() - 2);

        if (lastSlash == -1) {
            // We were at a drive root (e.g. "E:/"), go back to drive selection
            new Thread(new Runnable() {
                public void run() {
                    showRoots();
                }
            }).start();
        } else {
            String parent = currentPath.substring(0, lastSlash + 1);
            openDirectory(parent);
        }
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cmdCancel) {
            listener.onBrowserCancelled();
            return;
        }

        if (c == cmdBack) {
            goUp();
            return;
        }

        if (c == cmdSelect || c == List.SELECT_COMMAND) {
            int index = getSelectedIndex();
            if (index == -1) return;

            // Handle "(Empty)" item
            if (fileList.size() <= index) return;

            String selectedName = (String) fileList.elementAt(index);
            int type = ((Integer) typeList.elementAt(index)).intValue();

            if (type == 0) {
                openDirectory(currentPath + selectedName);
            } else {
                // Return full URL
                String fullUrl = "file:///" + currentPath + selectedName;
                listener.onFileSelected(fullUrl, selectedName);
            }
        }
    }
}