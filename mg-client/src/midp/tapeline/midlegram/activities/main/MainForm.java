package midp.tapeline.midlegram.activities.main;

import midp.tapeline.midlegram.G;
import midp.tapeline.midlegram.activities.about.AboutForm;
import midp.tapeline.midlegram.activities.folderlist.FolderListForm;
import midp.tapeline.midlegram.activities.search.SearchChatsForm;
import midp.tapeline.midlegram.activities.settings.SettingsForm;
import midp.tapeline.midlegram.ui.Icons;
import midp.tapeline.midlegram.ui.base.ListActivity;

import javax.microedition.lcdui.Command;

public class MainForm extends ListActivity {

    private final Command selectCmd = new Command("Select", Command.ITEM, 1);

    public MainForm() {
        super("Midlegram", ListActivity.IMPLICIT);
        setSelectCommand(selectCmd);
        append("Folders", Icons.FOLDER);
        append("Search", Icons.SEARCH);
        append("Settings", Icons.SETTINGS);
        append("About", Icons.INFO);
        exitCommand = new Command("Exit", Command.EXIT, 1);
        addCommand(exitCommand);
    }

    protected void onCommand(Command cmd) {
        if (cmd == exitCommand) {
            G.midlet.exit();
        } else if (cmd == selectCmd) {
            switch (getSelectedIndex()) {
                case 0:
                    G.ui.startNew(new FolderListForm());
                    break;
                case 1:
                    G.ui.startNew(new SearchChatsForm());
                    break;
                case 2:
                    G.ui.startNew(new SettingsForm());
                    break;
                case 3:
                    G.ui.startNew(new AboutForm());
                    break;
            }
        }
    }

}
