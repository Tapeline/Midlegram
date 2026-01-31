package midp.tapeline.midlegram.activities.chatlist;

import midp.tapeline.midlegram.G;
import midp.tapeline.midlegram.StringUtils;
import midp.tapeline.midlegram.activities.chat.ChatForm;
import midp.tapeline.midlegram.state.data.Chat;
import midp.tapeline.midlegram.state.data.ChatFolder;
import midp.tapeline.midlegram.state.data.PaginatedChats;
import midp.tapeline.midlegram.ui.Icons;
import midp.tapeline.midlegram.ui.base.ListActivity;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;
import java.util.Vector;

public class ChatListForm extends ListActivity {

    private final ChatListController controller;
    private final ChatFolder folder;
    private Vector chats;
    private int page = 0;
    private int pageCount = 1;
    private final Command selectCmd = new Command("Select", Command.ITEM, 1);

    public ChatListForm(ChatFolder folder) {
        super("Chats | " + folder.name, List.IMPLICIT);
        this.folder = folder;
        controller = new ChatListController(this, folder);
        setFitPolicy(List.TEXT_WRAP_ON);
        setSelectCommand(selectCmd);
        addBackButton();
    }

    public void onCreate() {
        controller.init();
        reloadChats();
    }

    public void reloadChats() {
        setTicker(new Ticker("Loading...\nPlease wait"));
        PaginatedChats maybeChats = controller.maybeGetChats(page);
        if (maybeChats != null) updateChats(maybeChats);
    }

    public void onDestroy() {
        controller.deinit();
    }

    void updateChats(PaginatedChats newChats) {
        pageCount = newChats.totalPages;
        chats = newChats.chats;
        setTicker(null);
        deleteAll();
        if (page > 0)
            append("Previous\n" + getPageInfoString(), Icons.PREVIOUS);
        for (int i = 0; i < chats.size(); i++)
            append(
                getItemName((Chat) chats.elementAt(i)),
                Icons.MESSAGE
            );
        if (page + 1 < pageCount)
            append("Next\n" + getPageInfoString(), Icons.NEXT);
    }

    private String getItemName(Chat chat) {
        if (chat.unreadCount > 0)
            return "(+" + chat.unreadCount + ") " + chat.title + "\n" +
                StringUtils.trunc(chat.lastMessage, 16);
        else
            return chat.title + "\n" +
                StringUtils.trunc(chat.lastMessage, 16);
    }

    private String getPageInfoString() {
        return "(now page " + (page + 1) + " / " + pageCount + ")";
    }

    protected void onCommand(Command cmd) {
        if (cmd == selectCmd) {
            int selected = getSelectedIndex();
            if (page > 0 && selected == 0) {
                page--;
                reloadChats();
            } else if (page + 1 < pageCount && selected == size() - 1) {
                page++;
                reloadChats();
            } else {
                int chatPos = selected;
                if (page > 0) chatPos--;  // rm prev button
                if (page + 1 < pageCount) chatPos--;  // rm next button
                G.ui.startNew(new ChatForm(((Chat) chats.elementAt(chatPos))));
            }
        }
    }

}
