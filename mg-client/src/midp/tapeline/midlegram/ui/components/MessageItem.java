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
import midp.tapeline.midlegram.client.data.Message;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.forms.ChatFolderListForm;
import midp.tapeline.midlegram.ui.forms.ChatForm;
import midp.tapeline.midlegram.ui.forms.ChatListForm;

public class MessageItem extends StringItem {

	Message message;
	ChatForm form;
	
	public MessageItem(Message message, ChatForm form) {
		super(message.authorName, message.text + "\n\t" + StringUtils.dateToString(new Date(message.time)));
		this.form = form;
		this.message = message;
		setLayout(Item.LAYOUT_EXPAND);
	}
	
}
