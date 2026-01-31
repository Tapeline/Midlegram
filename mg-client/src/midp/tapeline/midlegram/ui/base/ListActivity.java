package midp.tapeline.midlegram.ui.base;

import javax.microedition.lcdui.*;

public class ListActivity
    extends List
    implements Activity, CommandListener, ItemCommandListener {

    protected Command exitCommand;
    protected UI ui;

    public ListActivity(String name, int listType) {
        super(name, listType);
        setCommandListener(this);
    }

    public final void setOwnedBy(UI ui) {
        this.ui = ui;
    }

    public void onDestroy() {

    }

    public void onPause() {

    }

    public void onResume() {

    }

    public void onCreate() {

    }

    protected void onCommand(Command cmd) {
    }

    protected final void addBackButton() {
        exitCommand = new Command("Back", Command.BACK, 1);
        addCommand(exitCommand);
    }

    public final void commandAction(Command cmd, Item arg1) {
        onCommand(cmd);
    }

    public final void commandAction(Command cmd, Displayable arg1) {
        if (exitCommand != null && cmd == exitCommand && ui.currentActivity() == this)
            ui.destroyCurrent();
        else
            onCommand(cmd);
    }

}
