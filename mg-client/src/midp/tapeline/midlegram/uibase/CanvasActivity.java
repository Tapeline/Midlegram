package midp.tapeline.midlegram.uibase;

import javax.microedition.lcdui.*;

public abstract class CanvasActivity
    extends Canvas
    implements Activity, CommandListener, ItemCommandListener {

    protected Command backCommand;
    protected UI ui;

    public CanvasActivity() {
        setCommandListener(this);
    }

    public void onCreate() {

    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onDestroy() {

    }

    public void setOwnedBy(UI ui) {
        this.ui = ui;
    }

    protected void onCommand(Command cmd) {
    }

    protected void addBackButton() {
        backCommand = new Command("Back", Command.BACK, 1);
        addCommand(backCommand);
    }

    public void commandAction(Command cmd, Item arg1) {
        onCommand(cmd);
    }

    public void commandAction(Command cmd, Displayable arg1) {
        if (backCommand != null && cmd == backCommand && ui.currentActivity() == this)
            ui.destroyCurrent();
        else
            onCommand(cmd);
    }

}
