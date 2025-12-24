package midp.tapeline.midlegram.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;

import midp.tapeline.midlegram.ui.components.LoadingItem;

public abstract class UIForm extends Form implements CommandListener, ItemCommandListener {

	private Command backCommand;
	private LoadingItem loading;
	
	public UIForm(String arg0) {
		super(arg0);
		setCommandListener(this);
	}
	
	public void onStart() {
		
	}
	public void onSuspend() {
		setLoading(false);
	}
	public void onResume() {
	}
	public void onEnd() {}
	protected void onCommand(Command cmd) {}
	
	protected void addBackButton() {
		backCommand = new Command("Back", Command.BACK, 1);
		addCommand(backCommand);
	}
	
	public void commandAction(Command cmd, Item arg1) {
		onCommand(cmd);
	}
	public void commandAction(Command cmd, Displayable arg1) {
		System.out.println(cmd);
		if (backCommand != null && cmd == backCommand) {
			UI.endCurrent();
		}
		onCommand(cmd);
	}
	
	public void setLoading(boolean isLoading) {
		if (isLoading == (loading != null)) return;
		if (isLoading) {
			loading = new LoadingItem();
			loading.setActive(true);
			loading.setIndeterminate();
			insert(0, loading);
		} else {
			delete(0);
			loading = null;
		}
	}

	public void deleteAll() {
		setLoading(false);
		super.deleteAll();
	}
}
