package midp.tapeline.midlegram.util;

import java.util.Vector;

public class Queue {

    private final Vector ins = new Vector();
    private final Vector del = new Vector();

    public Queue() {}

    public void put(Object object) {
        ins.addElement(object);
    }

    public Object pop() {
        if (del.isEmpty())
            while (!ins.isEmpty()) {
                del.addElement(ins.elementAt(ins.size() - 1));
                ins.removeElementAt(ins.size() - 1);
            }
        Object last = del.elementAt(del.size() - 1);
        del.removeElementAt(del.size() - 1);
        return last;
    }

    public boolean isEmpty() {
        return ins.isEmpty() && del.isEmpty();
    }

    public int size() {
        return ins.size() + del.size();
    }

}
