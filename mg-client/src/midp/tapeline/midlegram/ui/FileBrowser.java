package midp.tapeline.midlegram.ui;

import javax.microedition.lcdui.*;
import javax.microedition.io.file.*;
import javax.microedition.io.*;
import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;

// Vibecoded filebrowser
public class FileBrowser extends List implements CommandListener, Runnable {

    private String currentPath; // Current directory URL (e.g. file:///E:/Images/)
    private Vector fileList = new Vector(); // Stores real filenames
    private Vector typeList = new Vector(); // Stores type (0=dir, 1=file)
    
    private Command cmdSelect;
    private Command cmdBack;
    private Command cmdCancel;
    
    private FileSelectListener listener;
    private Display display;
    
    // Extensions to show
    private static final String[] VALID_EXTENSIONS = {
        ".jpg", ".jpeg", ".png", ".gif", // Images
    };

    private static final String ROOT = "/";
    private static final Image ICON_DIR = null; // Add image if you have resources
    private static final Image ICON_FILE = null;

    public FileBrowser(Display display, FileSelectListener listener) {
        super("Select File", List.IMPLICIT);
        this.display = display;
        this.listener = listener;
        
        cmdSelect = new Command("Select", Command.ITEM, 1);
        cmdBack = new Command("Back", Command.BACK, 1);
        cmdCancel = new Command("Cancel", Command.EXIT, 1);
        
        addCommand(cmdSelect);
        addCommand(cmdBack); // Acts as "Up Directory" or "Cancel" if at root
        setCommandListener(this);
        setSelectCommand(cmdSelect); // Allow clicking center button/tapping
        
        // Start at root
        showRoots();
    }

    private void showRoots() {
        currentPath = ROOT;
        deleteAll();
        fileList.removeAllElements();
        typeList.removeAllElements();
        setTitle("Select Drive");

        Enumeration drives = FileSystemRegistry.listRoots();
        while (drives.hasMoreElements()) {
            String root = (String) drives.nextElement();
            // On Nokia E7, E:/ is usually where user data is
            append(root, ICON_DIR); 
            fileList.addElement(root);
            typeList.addElement(new Integer(0)); // 0 = Dir
        }
    }

    private void openDirectory(String url) {
        // Run in thread to avoid freezing UI during IO
        currentPath = url;
        new Thread(this).start();
    }

    public void run() {
        // This runs when opening a directory
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open("file:///" + currentPath, Connector.READ);
            Enumeration list = fc.list();
            
            // Prepare UI updates (Batching for safety)
            final Vector safeFiles = new Vector();
            final Vector safeTypes = new Vector();
            
            while (list.hasMoreElements()) {
                String filename = (String) list.nextElement();
                boolean isDir = filename.endsWith("/");
                
                if (isDir) {
                    safeFiles.addElement(filename);
                    safeTypes.addElement(new Integer(0));
                } else {
                    // Filter extensions
                    if (isValidMedia(filename)) {
                        safeFiles.addElement(filename);
                        safeTypes.addElement(new Integer(1));
                    }
                }
            }

            // Update UI on main thread
            final String pathTitle = currentPath;
            display.callSerially(new Runnable() {
                public void run() {
                    setTitle(pathTitle);
                    deleteAll();
                    fileList.removeAllElements();
                    typeList.removeAllElements();
                    
                    for (int i = 0; i < safeFiles.size(); i++) {
                        String f = (String) safeFiles.elementAt(i);
                        Integer t = (Integer) safeTypes.elementAt(i);
                        
                        // Visual trick: Directories usually look better with brackets or icon
                        String label = (t.intValue() == 0) ? "[" + f + "]" : f;
                        append(label, t.intValue() == 0 ? ICON_DIR : ICON_FILE);
                        
                        fileList.addElement(f);
                        typeList.addElement(t);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            // If error (access denied), go back
            goUp();
        } finally {
            try { if (fc != null) fc.close(); } catch (IOException ignored) {}
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
        
        // Logic to strip last folder
        // "E:/Images/Camera/" -> "E:/Images/"
        int lastSlash = currentPath.lastIndexOf('/', currentPath.length() - 2);
        if (lastSlash == -1) {
            showRoots();
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

            String selectedName = (String) fileList.elementAt(index);
            int type = ((Integer) typeList.elementAt(index)).intValue();

            if (type == 0) {
                // It's a directory
                openDirectory(currentPath + selectedName);
            } else {
                // It's a file
                String fullUrl = "file:///" + currentPath + selectedName;
                listener.onFileSelected(fullUrl, selectedName);
            }
        }
    }
    
    public interface FileSelectListener {
        void onFileSelected(String url, String filename);
        void onBrowserCancelled();
    }
    
}