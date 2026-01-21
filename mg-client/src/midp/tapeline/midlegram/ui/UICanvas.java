package midp.tapeline.midlegram.ui;

import javax.microedition.lcdui.*;

public abstract class UICanvas extends Canvas implements CommandListener, ItemCommandListener, UIDisplayable {

    protected Command backCommand;
    private boolean isLoading = false;

    public UICanvas() {
        setCommandListener(this);
    }

    public void onStart() {

    }

    public void onSuspend() {
        setLoading(false);
    }

    public void onResume() {
    }

    public void onEnd() {
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
        System.out.println(cmd);
        if (backCommand != null && cmd == backCommand) {
            UI.endCurrent();
        }
        onCommand(cmd);
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    protected void paint(Graphics g) {
        if (!isLoading) return;
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, g.getClipWidth(), g.getClipHeight());
        int x = (int) ((Animation.t * 10) % g.getClipWidth());
        g.setColor(63, 63, 255);
        g.fillRect(x, 0, x + 32, g.getClipHeight());
    }

}
