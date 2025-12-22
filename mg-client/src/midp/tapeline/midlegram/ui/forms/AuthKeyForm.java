package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.Settings;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;

public class AuthKeyForm extends UIForm {
	
	Command next = new Command("Next", Command.OK, 1);
	Command logInWithPhone = new Command("Log in with phone", Command.SCREEN, 1);
	TextField keyField = new TextField("Session key", "", 36, TextField.ANY);

	public AuthKeyForm() {
		super("Log in with key");
		append(keyField);
		addCommand(logInWithPhone);
		addCommand(next);
		keyField.setLayout(Item.LAYOUT_EXPAND);
		addBackButton();
	}
	
	protected void onCommand(Command cmd) {
		if (cmd == next) {
			Settings.sessionKey = keyField.getString();
			Settings.save();
			UI.startFormFromScratch(new ChatFolderListForm());
		} else if (cmd == logInWithPhone) {
			UI.startFormFromScratch(new StartAuthForm());
		}
	}

}
