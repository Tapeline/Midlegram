package midp.tapeline.midlegram.client;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import midp.tapeline.midlegram.client.data.Chat;
import midp.tapeline.midlegram.client.data.ChatFolder;

public class Telegram {
	
	private static int MESSAGE_BATCH = 5;
	
	MGClient client;
	Hashtable chats;
	Vector folders;
	Hashtable foldersToChatIds;
	Hashtable messages;

	public Telegram(MGClient client) {
		this.client = client;
		this.chats = null;
		this.folders = null;
		this.foldersToChatIds = null;
		this.messages = new Hashtable();
	}
	
	public void connect() throws IOException {
		client.connectClient();
	}
	
	public void startAuth(String phone) throws IOException {
		client.startAuth(phone);
	}
	
	public void confirmCode(String code) throws IOException {
		client.confirmCode(code);
	}
	
	public Vector getFolders() throws IOException {
		if (folders == null || foldersToChatIds == null) {
			folders = client.getFolders();
			foldersToChatIds = new Hashtable();
			for (int i = 0; i < folders.size(); ++i)
				foldersToChatIds.put(
						new Long(((ChatFolder) folders.elementAt(i)).id), 
						client.getChatsIds(((ChatFolder) folders.elementAt(i)).id)
				);
		}
		return folders;
	}
	
	public Vector getChats(long folderId) throws IOException {
		if (folders == null || 
			foldersToChatIds == null || 
			!foldersToChatIds.containsKey(new Long(folderId))) 
			getFolders();
		Vector ids = (Vector) foldersToChatIds.get(new Long(folderId));
		Vector folderChats = new Vector();
		for (int i = 0; i < ids.size(); ++i)
			folderChats.addElement(getChat((Long) ids.elementAt(i)));
		return folderChats;
	}
	
	public Chat getChat(Long chatId) throws IOException {
		if (chats == null) chats = new Hashtable();
		if (!messages.containsKey(chatId)) messages.put(chatId, new Vector());
		if (chats.containsKey(chatId)) return (Chat) chats.get(chatId);
		Chat chat = client.getChat(chatId.longValue());
		chats.put(chatId, chat);
		return chat;
	}
	
	public Vector getMessages(Long chatId, long fromMsg) throws IOException {
		return client.getMessages(chatId.longValue(), fromMsg, MESSAGE_BATCH);
	}
	
	public void sendTextMessage(long chatId, String message) throws IOException {
		client.sendTextMessage(chatId, message);
	}
	
	public byte[] getFile(int id, String mimetype) throws IOException {
		return client.getFile(id, mimetype);
	}
	
}
