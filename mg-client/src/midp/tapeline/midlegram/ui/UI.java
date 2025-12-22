package midp.tapeline.midlegram.ui;

import java.util.Vector;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;

import midp.tapeline.midlegram.Midlegram;

public class UI {

	private static Vector formStack = new Vector();
	private static Alert currentAlert = null;
	
	public static UIForm currentForm() {
		if (formStack.size() == 0)
			return null;
		return ((UIForm) formStack.elementAt(formStack.size() - 1));
	}
	
	public static void startForm(final UIForm form) {
		if (formStack.size() > 0)
			currentForm().onSuspend();
		formStack.addElement(form);
		Display.getDisplay(Midlegram.instance).setCurrent(form);
		Display.getDisplay(Midlegram.instance).getCurrent();
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {}
				form.onStart();
			}
		}).start();
		
	}
	
	public static void startFormFromScratch(UIForm form) {
		for (int i = 0; i < formStack.size(); i++) {
			currentForm().onEnd();
			popCurrent();
		}
		startForm(form);
	}
	
	private static void popCurrent() {
		formStack.removeElementAt(formStack.size() - 1);
	}
	
	public static void endCurrent() {
		if (formStack.size() == 1) {
			Midlegram.instance.exit();
		} else {
			currentForm().onEnd();
			popCurrent();
			Display.getDisplay(Midlegram.instance).setCurrent(currentForm());
			currentForm().onResume();
		}
	}
	
	public static final void alertFatal(String content) {
		currentAlert = new Alert("Error", content, null, AlertType.ERROR);
		Display.getDisplay(Midlegram.instance).setCurrent(currentAlert);
	}
	
	public static final void alertFatal(Exception exc) {
		currentAlert = new Alert("Error", exc.toString(), null, AlertType.ERROR);
		Display.getDisplay(Midlegram.instance).setCurrent(currentAlert);
		exc.printStackTrace();
	}
	
	public static final void ensureNoAlerts() {
			
	}
	
	public static final void alertInfo(String content) {
		if (Display.getDisplay(Midlegram.instance).getCurrent() instanceof Alert) {
			currentAlert = (Alert) Display.getDisplay(Midlegram.instance).getCurrent();
			currentAlert.setString(content);
			currentAlert.setTitle("Info");
			currentAlert.setType(AlertType.INFO);
			return;
		}
		currentAlert = new Alert("Info", content, null, AlertType.INFO);
		Display.getDisplay(Midlegram.instance).setCurrent(currentAlert);
	}
	
}
