package midp.tapeline.midlegram.events;

public class BackgroundMessageDeliverer implements Runnable {

    private Thread thread;
    private final MessageBus bus;
    private boolean isRunning;
    private final int delay;

    public BackgroundMessageDeliverer(MessageBus bus, int delay) {
        this.bus = bus;
        this.delay = delay;
    }

    public void start() {
        if (isRunning) return;
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    public void stop() {
        if (!isRunning) return;
        isRunning = false;
        try {
            thread.join();
            thread = null;
        } catch (InterruptedException ignored) {}
    }

    public void run() {
        while (isRunning) {
            bus.handleAll();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ignored) {}
        }
    }

}
