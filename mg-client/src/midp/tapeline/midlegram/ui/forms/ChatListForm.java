package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.client.Telegram.ChatListPage;
import midp.tapeline.midlegram.client.data.Chat;
import midp.tapeline.midlegram.client.data.ChatFolder;
import midp.tapeline.midlegram.client.data.Message;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;
import midp.tapeline.midlegram.ui.components.ChatItem;
import midp.tapeline.midlegram.ui.components.LoadingItem;
import midp.tapeline.midlegram.ui.components.MessageItem;

public class ChatListForm extends UIForm {

    private static final int PAGE_SIZE = 10;

    ChatFolder folder;
    Command prev = new Command("Prev", Command.SCREEN, 1);
    Command next = new Command("Next", Command.SCREEN, 1);
    StringItem prevButton = new StringItem("", "Previous", Item.BUTTON);
    StringItem nextButton = new StringItem("", "Next", Item.BUTTON);
    int currentPage = 0;
    ChatListPage chats;

    public ChatListForm(ChatFolder folder) {
        super("Chats | " + folder.name);
        this.folder = folder;
        prevButton.setLayout(Item.LAYOUT_EXPAND);
        nextButton.setLayout(Item.LAYOUT_EXPAND);
        prevButton.setDefaultCommand(prev);
        nextButton.setDefaultCommand(next);
        prevButton.setItemCommandListener(this);
        nextButton.setItemCommandListener(this);
        addBackButton();
    }
	
	/*private void repaintChats() {
		setLoading(false);
		deleteAll();
		setLoading(true);
		if (currentPage > 0)
			append(prevButton);
		for (
			int i = currentPage * PAGE_SIZE; 
			i < Math.min((currentPage + 1) * PAGE_SIZE, chats.size()); 
			++i
		) 
			append(new ChatItem((Chat) chats.elementAt(i)));
		if ((currentPage + 1) * PAGE_SIZE < chats.size()) append(nextButton);
		setLoading(false);
	}*/

    private void repaintChats() {
        setLoading(false);
        deleteAll();
        setLoading(true);
        if (currentPage > 0)
            append(prevButton);
        for (int i = 0; i < chats.chats.size(); ++i)
            append(new ChatItem((Chat) chats.chats.elementAt(i)));
        if (chats.hasNext) append(nextButton);
        setLoading(false);
    }

    private void reloadChats() {
        deleteAll();
        setLoading(true);
        new Thread(new Runnable() {
            public void run() {
                try {
                    chats = Services.tg.getChats(folder.id, currentPage * 5, 5);
                    repaintChats();
                } catch (IOException exc) {
                    UI.alertFatal(exc);
                } finally {
                    setLoading(false);
                }
            }
        }).start();
    }

    public void onStart() {
        reloadChats();
    }

    protected void onCommand(Command cmd) {
        if (cmd == prev) {
            currentPage--;
            reloadChats();
        } else if (cmd == next) {
            currentPage++;
            reloadChats();
        }
    }

    public void onResume() {
        setLoading(false);
    }

}
