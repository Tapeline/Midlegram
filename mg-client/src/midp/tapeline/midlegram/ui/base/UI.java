package midp.tapeline.midlegram.ui.base;

import java.util.Vector;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;

public class UI {

    private static final int PARALLEL_ACTION_DELAY_MS = 10;
    private Vector stack = new Vector();
    private MIDlet midlet;
    private Exitable exitable;
    private Displayable overlay = null;

    public UI(MIDlet midlet) {
        this.midlet = midlet;
        if (!(midlet instanceof Exitable))
            throw new IllegalArgumentException("Not a midlet provided");
        this.exitable = (Exitable) midlet;
    }

    private void popCurrent() {
        stack.removeElementAt(stack.size() - 1);
    }

    private void displaySetCurrent(Activity activity) {
        if (!(activity instanceof Displayable))
            throw new ActivityManagementError(
                "Tried to set current Activity that's not a Displayable"
            );
        Display display = Display.getDisplay(midlet);
        if (display == null)
            throw new ActivityManagementError("display == null");
        display.setCurrent((Displayable) activity);
    }

    public Activity currentActivity() {
        if (stack.isEmpty())
            return null;
        return ((Activity) stack.elementAt(stack.size() - 1));
    }

    public void startNew(final Activity activity) {
        if (!stack.isEmpty())
            pauseCurrent();
        activity.setOwnedBy(this);
        stack.addElement(activity);
        displaySetCurrent(activity);
        new ParallelAction() {
            void action() {
                activity.onCreate();
                activity.onResume();
            }
        }.start();
    }

    public void destroyAllStartNew(final Activity activity) {
        destroyAll();
        startNew(activity);
    }

    public void destroyAll() {
        while (!stack.isEmpty())
            destroyCurrent();
    }

    public void pauseCurrent() {
        if (stack.isEmpty())
            throw new ActivityManagementError("No current activity to pause");
        new ParallelAction() {
            void action() {
                currentActivity().onPause();
            }
        }.start();
    }

    public void destroyCurrent() {
        if (stack.isEmpty())
            throw new ActivityManagementError("No current activity to destroy");
        final Activity destroyed = currentActivity();
        popCurrent();
        new ParallelAction() {
            void action() {
                destroyed.onPause();
                destroyed.onDestroy();
            }
        }.start();
        if (!stack.isEmpty()) {
            displaySetCurrent(currentActivity());
            new ParallelAction() {
                void action() {
                    currentActivity().onResume();
                }
            }.start();
        } else
            exitable.exit();
    }

    public void restoreFromOverlay() {
        if (overlay == null) return;
        displaySetCurrent(currentActivity());
    }

    public void setOverlay(Displayable disp) {
        if (overlay != null) restoreFromOverlay();
        overlay = disp;
        Display display = Display.getDisplay(midlet);
        if (display == null)
            throw new ActivityManagementError("display == null");
        display.setCurrent(disp);
    }

    static abstract class ParallelAction extends Thread {

        abstract void action();

        public void run() {
            try {
                Thread.sleep(PARALLEL_ACTION_DELAY_MS);
            } catch (InterruptedException e) {
            }
            action();
        }

    }

}
