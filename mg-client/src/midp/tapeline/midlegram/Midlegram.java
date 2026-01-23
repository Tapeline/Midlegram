package midp.tapeline.midlegram;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import midp.tapeline.midlegram.client.MGClient;
import midp.tapeline.midlegram.logging.Logger;
import midp.tapeline.midlegram.ui.Splash;
import midp.tapeline.midlegram.uibase.UI;
import midp.tapeline.midlegram.ui.forms.AboutForm;

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
        G.ui.startNew(new AboutForm());
    }

}
