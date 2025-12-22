package midp.tapeline.midlegram.ui.components;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import midp.tapeline.midlegram.StringUtils;
import midp.tapeline.midlegram.client.data.Chat;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.forms.ChatFolderListForm;
import midp.tapeline.midlegram.ui.forms.ChatForm;
import midp.tapeline.midlegram.ui.forms.ChatListForm;

public class ChatItem extends StringItem implements ItemCommandListener {

	Chat chat;
	ChatListForm listForm;
	Command go = new Command("Go", Command.OK, 1);
	
	public ChatItem(Chat chat, ChatListForm listForm) {
		super(
				chat.title, 
				(chat.unreadCount > 0? "(" + chat.unreadCount + ") " : "") 
				+ StringUtils.trunc(chat.lastMessage, 32)
		);
		this.chat = chat;
		this.listForm = listForm;
		setDefaultCommand(go);
		setItemCommandListener(this);
		setLayout(Item.LAYOUT_EXPAND);
	}

	public void commandAction(Command cmd, Item arg1) {
		if (cmd == go) {
			UI.startForm(new ChatForm(chat));
		}
	}
	
}
