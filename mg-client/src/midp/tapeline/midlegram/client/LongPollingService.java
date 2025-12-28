package midp.tapeline.midlegram.client;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.client.data.Message;
import midp.tapeline.midlegram.ui.UI;

import java.util.Vector;


public class LongPollingService implements Runnable {
    
    private volatile boolean running = false;
    private Thread workerThread;

    public void start() {
        if (running) return;
        running = true;
        workerThread = new Thread(this);
        workerThread.setPriority(Thread.MIN_PRIORITY); 
        //workerThread.start();
    }

    public void stop() {
        running = false;
    }

    public void run() {
        while (running) {
            try {
            	Vector updates = Services.client.pollUpdates();
            	for (int i = 0; i < updates.size(); i++) {
					UI.alertInfo(((Message) updates.elementAt(i)).toString());
				}
            } catch (Exception e) {
                e.printStackTrace();
            	// wait before retry
                try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
            }
        }
    }
}