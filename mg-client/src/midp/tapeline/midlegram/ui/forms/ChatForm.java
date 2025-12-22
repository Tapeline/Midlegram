package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;
import java.util.Vector;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.client.data.Chat;
import midp.tapeline.midlegram.client.data.Message;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;
import midp.tapeline.midlegram.ui.components.ChatItem;
import midp.tapeline.midlegram.ui.components.LoadingItem;
import midp.tapeline.midlegram.ui.components.MessageItem;

public class ChatForm extends UIForm {

	LoadingItem loading;
	Chat chat;
	
	public ChatForm(Chat chat) {
		super(chat.title);
		this.chat = chat;
		addBackButton();
	}
	
	public void onStart() {
		setLoading(true);
		try {
			Vector messages = Services.tg.getMessages(new Long(chat.id), 0);
			for (int i = messages.size() - 1; i >= 0; --i) 
				append(new MessageItem((Message) messages.elementAt(i), this));
		} catch (IOException exc) {
			UI.alertFatal(exc);
		} finally {
			setLoading(false);
		}
	}

}
