package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;

public class StartAuthForm extends UIForm {
	
	Command next = new Command("Next", Command.OK, 1);
	Command logInWithKey = new Command("Log in with key", Command.SCREEN, 1);
	TextField phoneField = new TextField("Phone", "+79869667927", 20, TextField.PHONENUMBER);

	public StartAuthForm() {
		super("Log in with phone");
		append(phoneField);
		addCommand(logInWithKey);
		addCommand(next);
		addBackButton();
		phoneField.setLayout(Item.LAYOUT_EXPAND);
	}
	
	protected void onCommand(Command cmd) {
		if (cmd == next) {
			try {
				Services.tg.startAuth(phoneField.getString());
				UI.startForm(new AuthCodeForm());
			} catch (IOException exc) {
				UI.alertFatal(exc);
			}
		} else if (cmd == logInWithKey) {
			UI.startFormFromScratch(new AuthKeyForm());
		}
	}

}
