package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.Settings;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;

public class StartAuthForm extends UIForm implements Runnable {

    Command next = new Command("Next", Command.OK, 1);
    Command logInWithKey = new Command("Log in with key", Command.SCREEN, 1);
    TextField phoneField = new TextField("Phone", "+79869667927", 20, TextField.PHONENUMBER);

    public StartAuthForm() {
        super("Log in with phone");
        append(phoneField);
        addCommand(logInWithKey);
        addCommand(next);
        addBackButton();
        phoneField.setLayout(Item.LAYOUT_EXPAND);
    }

    protected void onCommand(Command cmd) {
        if (cmd == next) {
            setLoading(true);
            new Thread(this).start();
        } else if (cmd == logInWithKey) {
            UI.startFormFromScratch(new AuthKeyForm());
        }
    }

    public void run() {
        try {
            Services.tg.startAuth(phoneField.getString());
            Settings.sessionKey = Services.tg.getSessionKey();
            Settings.save();
            UI.startForm(new AuthCodeForm());
        } catch (IOException exc) {
            UI.alertFatal(exc);
        } finally {
            setLoading(false);
        }
    }

}
