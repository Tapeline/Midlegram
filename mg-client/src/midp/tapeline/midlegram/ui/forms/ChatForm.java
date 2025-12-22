package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.client.data.Chat;
import midp.tapeline.midlegram.client.data.Message;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;
import midp.tapeline.midlegram.ui.components.ChatItem;
import midp.tapeline.midlegram.ui.components.LoadingItem;
import midp.tapeline.midlegram.ui.components.MessageItem;

public class ChatForm extends UIForm {

	Vector messages;
	LoadingItem loading;
	Chat chat;
	Command prev = new Command("Prev", Command.SCREEN, 1);
	Command next = new Command("Next", Command.SCREEN, 1);
	Command reload = new Command("Reload", Command.SCREEN, 1);
	Command send = new Command("Send", Command.SCREEN, 1);
	StringItem prevButton = new StringItem("", "Earlier", Item.BUTTON);
	StringItem nextButton = new StringItem("", "Later", Item.BUTTON);
	TextField msgInput = new TextField("New message", "", 1000, TextField.ANY);
	Vector anchors = new Vector();
	int currentAnchor = 0;
	
	public ChatForm(Chat chat) {
		super(chat.title);
		this.chat = chat;
		prevButton.setLayout(Item.LAYOUT_EXPAND);
		nextButton.setLayout(Item.LAYOUT_EXPAND);
		prevButton.setDefaultCommand(prev);
		nextButton.setDefaultCommand(next);
		prevButton.setItemCommandListener(this);
		nextButton.setItemCommandListener(this);
		msgInput.setLayout(Item.LAYOUT_EXPAND);
		msgInput.setItemCommandListener(this);
		msgInput.setDefaultCommand(send);
		addCommand(send);
		addCommand(reload);
		addBackButton();
		anchors.addElement(new Long(0));
	}
	
	private void reloadMessages() {
		deleteAll();
		setLoading(true);
		try {
			Long fromId = (Long) anchors.elementAt(currentAnchor);
			messages = Services.tg.getMessages(new Long(chat.id), fromId.longValue());
			append(prevButton);
			for (int i = messages.size() - 1; i >= 0; --i) 
				append(new MessageItem((Message) messages.elementAt(i), this));
			if (currentAnchor != 0) append(nextButton);
			append(msgInput);
		} catch (IOException exc) {
			UI.alertFatal(exc);
		} finally {
			setLoading(false);
		}
	}
	
	public void onStart() {
		reloadMessages();
	}

	protected void onCommand(Command cmd) {
		if (cmd == prev) {
			anchors.addElement(new Long(((Message) messages.elementAt(messages.size() - 1)).id));
			currentAnchor++;
			new Thread(new Runnable() {
				public void run() {
					reloadMessages();
				}
			}).start(); 
		} else if (cmd == next) {
			anchors.removeElementAt(anchors.size() - 1);
			currentAnchor--;
			new Thread(new Runnable() {
				public void run() {
					reloadMessages();
				}
			}).start(); 
		} else if (cmd == reload) {
			new Thread(new Runnable() {
				public void run() {
					reloadMessages();
				}
			}).start(); 
		} else if (cmd == send) {
			if (msgInput.getString().length() == 0) return;
			try {
				Services.tg.sendTextMessage(chat.id, msgInput.getString());
			} catch (IOException e) {
				UI.alertFatal(e);
			}
		}
	}
	

}
