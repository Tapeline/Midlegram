package midp.tapeline.midlegram.uibase;

import javax.microedition.lcdui.*;

public class FormActivity
    extends Form
    implements Activity, CommandListener, ItemCommandListener {

    protected Command backCommand;
    protected UI ui;

    public FormActivity(String name) {
        super(name);
        setCommandListener(this);
    }

    public void setOwnedBy(UI ui) {
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

    public void delete(Item item) {
        for (int i = 0; i < size(); i++) {
            if (get(i) == item) {
                delete(i);
                return;
            }
        }
    }

}
