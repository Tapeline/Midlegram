package midp.tapeline.midlegram.ui.components;

import java.util.Date;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import midp.tapeline.midlegram.StringUtils;
import midp.tapeline.midlegram.client.data.Chat;
import midp.tapeline.midlegram.client.data.Media;
import midp.tapeline.midlegram.client.data.Message;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.forms.ChatFolderListForm;
import midp.tapeline.midlegram.ui.forms.ChatForm;
import midp.tapeline.midlegram.ui.forms.ChatListForm;
import midp.tapeline.midlegram.ui.forms.MediaForm;

public class MessageItem extends StringItem implements ItemCommandListener {

	Message message;
	ChatForm form;
	Command viewMedia;
	
	public MessageItem(Message message, ChatForm form) {
		super(message.authorName, message.text + "\n\t" + 
					StringUtils.dateToString(new Date(((long) message.time) * 1000)));
		this.form = form;
		this.message = message;
		setLayout(Item.LAYOUT_EXPAND);
		setItemCommandListener(this);
		if (message.media.size() != 0) {
			viewMedia = new Command("View media", Command.SCREEN, 1);
			addCommand(viewMedia);
		}
	}

	public void commandAction(Command cmd, Item arg1) {
		if (cmd == viewMedia) {
			UI.startForm(new MediaForm((Media) message.media.elementAt(0)));
		}
	}
	
}
