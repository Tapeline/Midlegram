package midp.tapeline.midlegram;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import midp.tapeline.midlegram.client.MGClient;
import midp.tapeline.midlegram.filesystem.BinaryReader;
import midp.tapeline.midlegram.filesystem.FS;
import midp.tapeline.midlegram.logging.Logger;
import midp.tapeline.midlegram.ui.Splash;
import midp.tapeline.midlegram.ui.forms.DebugVLSForm;
import midp.tapeline.midlegram.uibase.UI;
import midp.tapeline.midlegram.ui.forms.AboutForm;

import java.io.IOException;
import java.io.InputStream;

public class Midlegram extends MIDlet implements Runnable {

    public Midlegram() {
        G.midlet = this;
    }

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {

    }

    protected void pauseApp() {

    }

    protected void startApp() throws MIDletStateChangeException {
        Display.getDisplay(this).setCurrent(new Splash());
        G.ui = new UI(this);
        Settings.load();
        G.logger = new Logger(true);
        G.client = new MGClient("http://midlegram.tapeline.dev", Settings.sessionKey);
        new Thread(this).start();
    }

    public void exit() {
        try {
            destroyApp(true);
        } catch (MIDletStateChangeException e) {
            throw new RuntimeException(e.toString());
        }
        notifyDestroyed();
    }

    public void run() {
        try {
            System.out.println("TOC: ");
            ArrayUtils.printArray(
                FS.readFully("file:///E:/vlstest/toc.dat")
            );
            System.out.println("IDX0: ");
            ArrayUtils.printArray(
                FS.readFully("file:///E:/vlstest/idx0.dat")
            );
            System.out.println("PAGE0: ");
            ArrayUtils.printArray(
                FS.readFully("file:///E:/vlstest/page0.dat")
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        G.ui.startNew(new DebugVLSForm());
    }

}
