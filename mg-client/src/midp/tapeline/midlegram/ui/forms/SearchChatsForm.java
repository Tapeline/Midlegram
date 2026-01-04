package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;
import java.util.Calendar;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.client.data.Chat;
import midp.tapeline.midlegram.client.data.ChatFolder;
import midp.tapeline.midlegram.client.data.Message;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;
import midp.tapeline.midlegram.ui.components.ChatItem;
import midp.tapeline.midlegram.ui.components.LoadingItem;
import midp.tapeline.midlegram.ui.components.MessageItem;

public class SearchChatsForm extends UIForm {

    Command search = new Command("Search", Command.SCREEN, 1);
    StringItem searchButton = new StringItem("", "Search", Item.BUTTON);
    TextField searchField = new TextField("Query", "", 100, TextField.ANY);
    Vector chats;

    public SearchChatsForm() {
        super("Search chats");
        searchButton.setLayout(Item.LAYOUT_EXPAND);
        searchField.setLayout(Item.LAYOUT_EXPAND);
        searchButton.setDefaultCommand(search);
        searchButton.setItemCommandListener(this);
        append(searchField);
        append(searchButton);
        addBackButton();
    }

    private void repaintChats() {
        setLoading(false);
        deleteAll();
        setLoading(true);
        append(searchField);
        append(searchButton);
        for (int i = 0; i < chats.size(); i++)
            append(new ChatItem((Chat) chats.elementAt(i)));
        setLoading(false);
    }

    protected void onCommand(Command cmd) {
        if (cmd == search) {
            setLoading(true);
            try {
                chats = Services.tg.searchChats(searchField.getString(), 10);
                repaintChats();
            } catch (IOException exc) {
                UI.alertFatal(exc);
            } finally {
                setLoading(false);
            }
        }
    }

}
