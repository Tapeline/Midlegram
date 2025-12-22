package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;
import java.util.Vector;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.client.data.Chat;
import midp.tapeline.midlegram.client.data.ChatFolder;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;
import midp.tapeline.midlegram.ui.components.ChatItem;
import midp.tapeline.midlegram.ui.components.LoadingItem;

public class ChatListForm extends UIForm {

	LoadingItem loading;
	ChatFolder folder;
	
	public ChatListForm(ChatFolder folder) {
		super("Chats | " + folder.name);
		this.folder = folder;
		addBackButton();
		loading = new LoadingItem();
		append(loading);
		loading.setActive(true);
		loading.setIndeterminate();
	}
	
	public void onStart() {
		try {
			Vector chats = Services.tg.getChats(folder.id);
			for (int i = 0; i < chats.size(); i++) 
				append(new ChatItem((Chat) chats.elementAt(i), this));
		} catch (IOException exc) {
			UI.alertFatal(exc);
		} finally {
			loading.setActive(false);
			delete(0);
		}
	}
	
	public void onResume() {
		setLoading(false);
	}

}
