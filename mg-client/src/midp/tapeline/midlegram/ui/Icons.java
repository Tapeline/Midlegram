package midp.tapeline.midlegram.ui;

import javax.microedition.lcdui.Image;
import java.io.IOException;

public class Icons {

    public static final Image FOLDER;
    public static final Image MESSAGE;
    public static final Image PREVIOUS;
    public static final Image NEXT;
    public static final Image SEARCH;
    public static final Image SETTINGS;
    public static final Image INFO;

    static {
        try {
            FOLDER = Image.createImage("/icons/folder.png");
            MESSAGE = Image.createImage("/icons/messaging.png");
            PREVIOUS = Image.createImage("/icons/previous.png");
            NEXT = Image.createImage("/icons/next.png");
            SEARCH = Image.createImage("/icons/search.png");
            SETTINGS = Image.createImage("/icons/settings.png");
            INFO = Image.createImage("/icons/info.png");
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

}
