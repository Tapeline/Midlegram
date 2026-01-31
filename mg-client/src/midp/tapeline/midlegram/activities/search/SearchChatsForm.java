package midp.tapeline.midlegram.activities.search;

import midp.tapeline.midlegram.G;
import midp.tapeline.midlegram.StringUtils;
import midp.tapeline.midlegram.activities.chat.ChatForm;
import midp.tapeline.midlegram.activities.chatlist.ChatListForm;
import midp.tapeline.midlegram.activities.folderlist.FolderListController;
import midp.tapeline.midlegram.state.data.Chat;
import midp.tapeline.midlegram.state.data.ChatFolder;
import midp.tapeline.midlegram.ui.Icons;
import midp.tapeline.midlegram.ui.base.ListActivity;

import javax.microedition.lcdui.*;
import java.util.Vector;

public class SearchChatsForm extends ListActivity {

    private String maybeQuery = null;
    private Vector results = new Vector();
    private final SearchChatsController controller;
    private final Command selectCmd = new Command("Select", Command.ITEM, 1);
    private final Command searchCmd = new Command("Search", Command.OK, 1);
    private final TextBox prompt = new TextBox("Query", "", 100, TextField.ANY);

    public SearchChatsForm() {
        super("Search", List.IMPLICIT);
        controller = new SearchChatsController(this);
        prompt.addCommand(searchCmd);
        prompt.setCommandListener(this);
        setSelectCommand(selectCmd);
        addBackButton();
    }

    public void onCreate() {
        controller.init();
        render();
    }

    public void onDestroy() {
        controller.deinit();
    }

    void updateResults(Vector results) {
        this.results = results;
        render();
    }

    private void render() {
        setTicker(null);
        deleteAll();
        if (maybeQuery == null)
            append("Tap to input query", Icons.SEARCH);
        else
            append(maybeQuery, Icons.SEARCH);
        for (int i = 0; i < results.size(); i++) {
            append(
                getItemName((Chat) results.elementAt(i)),
                Icons.MESSAGE
            );
        }
    }

    private String getItemName(Chat chat) {
        if (chat.unreadCount > 0)
            return "(+" + chat.unreadCount + ") " + chat.title + "\n" +
                StringUtils.trunc(chat.lastMessage, 16);
        else
            return chat.title + "\n" +
                StringUtils.trunc(chat.lastMessage, 16);
    }

    protected void onCommand(Command cmd) {
        if (cmd == selectCmd) {
            if (getSelectedIndex() == 0) {
                G.ui.setOverlay(prompt);
            } else {
                G.ui.startNew(new ChatForm(
                    ((Chat) results.elementAt(getSelectedIndex() - 1))
                ));
            }
        } else if (cmd == searchCmd) {
            setTicker(new Ticker("Loading"));
            maybeQuery = prompt.getString();
            G.ui.restoreFromOverlay();
            controller.search(maybeQuery);
        }
    }

}
