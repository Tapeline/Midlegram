package midp.tapeline.midlegram.ui.components;

import java.util.Date;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import midp.tapeline.midlegram.StringUtils;
import midp.tapeline.midlegram.client.data.AttachedMedia;
import midp.tapeline.midlegram.client.data.Chat;
import midp.tapeline.midlegram.client.data.Media;
import midp.tapeline.midlegram.client.data.Message;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.forms.ChatFolderListForm;
import midp.tapeline.midlegram.ui.forms.ChatForm;
import midp.tapeline.midlegram.ui.forms.ChatListForm;
import midp.tapeline.midlegram.ui.forms.MediaForm;

public class QueuedMediaItem extends StringItem implements ItemCommandListener {

	public final AttachedMedia media;
	ChatForm form;
	
	Command remove;
	
	public QueuedMediaItem(AttachedMedia media, ChatForm form) {
		super("Attached " + media.type, "tap to remove" , Item.PLAIN);
		this.form = form;
		this.media = media;
		setLayout(Item.LAYOUT_SHRINK);
		setItemCommandListener(this);
		remove = new Command("Remove", Command.SCREEN, 1);
		addCommand(remove);
		setDefaultCommand(remove);
		setItemCommandListener(this);
	}

	public void commandAction(Command cmd, Item arg1) {
		if (cmd == remove) {
			form.removeMediaToSend(media);
		}
	}
	
}
