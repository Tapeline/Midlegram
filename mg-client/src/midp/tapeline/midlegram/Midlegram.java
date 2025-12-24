package midp.tapeline.midlegram;

import java.io.IOException;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import midp.tapeline.midlegram.client.LongPollingService;
import midp.tapeline.midlegram.client.MGClient;
import midp.tapeline.midlegram.client.Telegram;
import midp.tapeline.midlegram.ui.Animation;
import midp.tapeline.midlegram.ui.Splash;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.forms.ChatFolderListForm;
import midp.tapeline.midlegram.ui.forms.StartAuthForm;

public class Midlegram extends MIDlet implements Runnable {

	public static Midlegram instance;
	
	public Midlegram() {
		instance = this;
		Services.longPoller = new LongPollingService();
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {

	}

	protected void pauseApp() {
		
	}

	protected void startApp() throws MIDletStateChangeException {
		Display.getDisplay(this).setCurrent(new Splash());
		Animation.startAnimations();
		Settings.load();
		if (!Settings.sessionKey.equals("c0e0af36-d51d-4a5a-ad8c-9360892945d1"))
			System.err.println("Key!");
		Services.client = new MGClient("http://mpgram.tapeline.dev", "c0e0af36-d51d-4a5a-ad8c-9360892945d1");
		Services.tg = new Telegram(Services.client);
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
		if (Settings.sessionKey != null) {
			try {
				Services.tg.connect();
			} catch (IOException e) {
				UI.alertFatal(e);
				UI.startFormFromScratch(new StartAuthForm());
				return;
			}
			UI.startFormFromScratch(new ChatFolderListForm());
		} else
			UI.startFormFromScratch(new StartAuthForm());
	}

}
