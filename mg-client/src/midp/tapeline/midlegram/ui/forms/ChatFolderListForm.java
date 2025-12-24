package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Command;

import midp.tapeline.midlegram.Midlegram;
import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.client.data.ChatFolder;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;
import midp.tapeline.midlegram.ui.components.ChatFolderItem;

public class ChatFolderListForm extends UIForm {
	
	Command exitCommand = new Command("Exit", Command.EXIT, 1);
	Command searchCommand = new Command("Search", Command.SCREEN, 1);
	
	public ChatFolderListForm() {
		super("Folders");
		addCommand(searchCommand);
		addCommand(exitCommand);
		setCommandListener(this);
	}
	
	public void onStart() {
		setLoading(true);
		try {
			Vector folders = Services.tg.getFolders();
			append(new ChatFolderItem(new ChatFolder(0, "All"), this));
			for (int i = 0; i < folders.size(); i++)
				append(new ChatFolderItem((ChatFolder) folders.elementAt(i), this));
			//Services.longPoller.start();
		} catch (IOException exc) {
			UI.alertFatal(exc);
		} finally {
			setLoading(false);
		}
	}
	
	public void onResume() {
		setLoading(false);
	}
	
	protected void onCommand(Command cmd) {
		if (cmd == exitCommand) {
			Midlegram.instance.exit();
		} else if (cmd == searchCommand) {
			UI.startForm(new SearchChatsForm());
		}
	}
	
}
