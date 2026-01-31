package midp.tapeline.midlegram.activities.folderlist;

import midp.tapeline.midlegram.G;
import midp.tapeline.midlegram.activities.chatlist.ChatListForm;
import midp.tapeline.midlegram.state.data.ChatFolder;
import midp.tapeline.midlegram.ui.Icons;
import midp.tapeline.midlegram.ui.base.ListActivity;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Ticker;
import java.util.Vector;

public class FolderListForm extends ListActivity {

    private Vector folders;
    private final FolderListController controller;
    private final Command selectCmd = new Command("Select", Command.ITEM, 1);

    public FolderListForm() {
        super("Folders", List.IMPLICIT);
        controller = new FolderListController(this);
        setSelectCommand(selectCmd);
        addBackButton();
    }

    public void onCreate() {
        controller.init();
        setTicker(new Ticker("Loading...\nPlease wait"));
        Vector maybeFolders = controller.maybeGetFolders();
        if (maybeFolders != null) updateFolders(maybeFolders);
    }

    public void onDestroy() {
        controller.deinit();
    }

    void updateFolders(Vector folders) {
        this.folders = folders;
        setTicker(null);
        deleteAll();
        for (int i = 0; i < folders.size(); i++)
            append(
                ((ChatFolder) folders.elementAt(i)).name,
                Icons.FOLDER
            );
    }

    protected void onCommand(Command cmd) {
        if (cmd == selectCmd) {
            G.ui.startNew(new ChatListForm(
                ((ChatFolder) folders.elementAt(getSelectedIndex()))
            ));
        }
    }

}
