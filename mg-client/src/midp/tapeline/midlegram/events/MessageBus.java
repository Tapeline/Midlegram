package midp.tapeline.midlegram.events;

import midp.tapeline.midlegram.util.Queue;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class MessageBus {

    private Hashtable subscribers = new Hashtable();
    private Queue queue = new Queue();
    private volatile long sequence;

    public synchronized void subscribe(Class key, Subscriber subscriber) {
        if (!subscribers.containsKey(key)) subscribers.put(key, new Vector());
        ((Vector) subscribers.get(key)).addElement(subscriber);
    }

    public synchronized void unsubscribe(Class key, Subscriber subscriber) {
        if (!subscribers.containsKey(key)) return;
        ((Vector) subscribers.get(key)).removeElement(subscriber);
    }

    public synchronized void unsubscribeAll(Subscriber subscriber) {
        Enumeration keys = subscribers.keys();
        while (keys.hasMoreElements())
            ((Vector) subscribers.get(keys.nextElement())).removeElement(subscriber);
    }

    public synchronized void publish(Message message) {
        queue.put(message);
    }

    public boolean hasUnread() {
        return !queue.isEmpty();
    }

    public synchronized Message pull() {
        return (Message) queue.pop();
    }

    public synchronized void handleOne() {
        Message message = pull();
        Vector subscribers = (Vector) this.subscribers.get(message.getClass());
        if (subscribers == null) return;
        for (int i = 0; i < subscribers.size(); i++)
            ((Subscriber) subscribers.elementAt(i)).onReceive(this, message);
    }

    public synchronized void handleAll() {
        while (hasUnread()) handleOne();
    }

    public synchronized long nextSeq() {
        return sequence++;
    }

    public Thread createBackgroundHandler(final int delay) {
        return new Thread() {
            public void run() {
                MessageBus.this.handleAll();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ignored) {}
            }
        };
    }

}
