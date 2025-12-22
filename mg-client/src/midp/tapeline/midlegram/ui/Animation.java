package midp.tapeline.midlegram.ui;

import java.util.Vector;

import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Item;

public class Animation {

	public static volatile long t = 0;
	private static Thread animThread;
	private static boolean running = false;
	private static Vector animated = new Vector();
	
	public static void startAnimations() {
		running = true;
		animThread = new Thread(new Runnable() {
			public void run() {
				while (running) {
					t++;
					try {
						Thread.sleep(50);
					} catch (InterruptedException ignored) {}
					for (int i = 0; i < animated.size(); ++i)
						((Repaintable) animated.elementAt(i)).update();
				}
			}
		});
		animThread.start();
	}
	
	public static void stopAnimations() {
		running = false;
		try {
			animThread.join();
		} catch (InterruptedException e) {}
	}
	
	public static void addAnimable(Repaintable item) {
		animated.addElement(item);
	}
	
	public static void removeAnimable(Repaintable item) {
		animated.removeElement(item);
	}
	
	public static interface Repaintable {
		void update();
	}

}
