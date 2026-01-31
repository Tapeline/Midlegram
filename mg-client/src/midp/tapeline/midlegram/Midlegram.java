package midp.tapeline.midlegram;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import midp.tapeline.midlegram.activities.folderlist.FolderListForm;
import midp.tapeline.midlegram.activities.main.MainForm;
import midp.tapeline.midlegram.client.AsyncClient;
import midp.tapeline.midlegram.logging.Logger;
import midp.tapeline.midlegram.testing.FakeTelegramClient;
import midp.tapeline.midlegram.ui.Splash;
import midp.tapeline.midlegram.ui.base.Exitable;
import midp.tapeline.midlegram.ui.base.UI;

public class Midlegram extends MIDlet implements Runnable, Exitable {

    public Midlegram() {
        G.midlet = this;
    }

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {

    }

    protected void pauseApp() {

    }

    protected void startApp() throws MIDletStateChangeException {
        Display.getDisplay(this).setCurrent(new Splash());
        Settings.load();
        G.ui = new UI(this);
        G.logger = new Logger(true);
        G.client = new FakeTelegramClient();
        G.asyncClient = new AsyncClient(G.client);
        G.asyncClient.subscribe(G.messageBus);
        G.deliverer.start();
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
        G.ui.startNew(new MainForm());
    }

}
