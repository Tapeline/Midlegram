package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;

public class AuthCodeForm extends UIForm {
	
	Command next = new Command("Next", Command.OK, 1);
	TextField codeField = new TextField("Auth code", "", 20, TextField.NUMERIC);

	public AuthCodeForm() {
		super("Log in with phone");
		append(codeField);
		addCommand(next);
		codeField.setLayout(Item.LAYOUT_EXPAND);
		addBackButton();
	}
	
	protected void onCommand(Command cmd) {
		if (cmd == next) {
			try {
				Services.tg.confirmCode(codeField.getString());
				UI.startFormFromScratch(new ChatFolderListForm());
			} catch (IOException exc) {
				UI.alertFatal(exc);
			}
		}
	}

}
