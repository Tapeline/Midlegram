package midp.tapeline.midlegram.ui.components;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import midp.tapeline.midlegram.client.data.AttachedMedia;
import midp.tapeline.midlegram.ui.forms.ChatForm;

public class QueuedMediaItem extends StringItem implements ItemCommandListener {

    public final AttachedMedia media;
    ChatForm form;

    Command remove;

    public QueuedMediaItem(AttachedMedia media, ChatForm form) {
        super("Attached " + media.type, "tap to remove", Item.PLAIN);
        this.form = form;
        this.media = media;
        setLayout(Item.LAYOUT_SHRINK);
        setItemCommandListener(this);
        remove = new Command("Remove", Command.SCREEN, 1);
        addCommand(remove);
        setDefaultCommand(remove);
        setItemCommandListener(this);
    }

    public void commandAction(Command cmd, Item arg1) {
        if (cmd == remove) {
            form.removeMediaToSend(media);
        }
    }

}
