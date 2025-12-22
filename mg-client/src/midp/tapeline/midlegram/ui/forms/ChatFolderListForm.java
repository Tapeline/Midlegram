package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;
import java.util.Vector;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.client.data.ChatFolder;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;
import midp.tapeline.midlegram.ui.components.ChatFolderItem;

public class ChatFolderListForm extends UIForm {
	
	public ChatFolderListForm() {
		super("Folders");
		addBackButton();
	}
	
	public void onStart() {
		setLoading(true);
		try {
			Vector folders = Services.client.getFolders();
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
	
}
