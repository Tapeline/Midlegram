package midp.tapeline.midlegram.ui.components;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import midp.tapeline.midlegram.client.data.ChatFolder;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.forms.ChatFolderListForm;
import midp.tapeline.midlegram.ui.forms.ChatListForm;

public class ChatFolderItem extends StringItem implements ItemCommandListener {

	ChatFolder folder;
	ChatFolderListForm folderListForm;
	Command go = new Command("Go", Command.OK, 1);
	
	public ChatFolderItem(ChatFolder chatFolder, ChatFolderListForm folderListForm) {
		super(chatFolder.name, Long.toString(chatFolder.id));
		this.folder = chatFolder;
		this.folderListForm = folderListForm;
		setDefaultCommand(go);
		setItemCommandListener(this);
		setLayout(Item.LAYOUT_EXPAND);
	}

	public void commandAction(Command cmd, Item arg1) {
		if (cmd == go) {
			UI.startForm(new ChatListForm(folder));
		}
	}
	
}
