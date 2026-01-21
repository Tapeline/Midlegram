package midp.tapeline.midlegram.ui.components;

import java.util.Date;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;

import midp.tapeline.midlegram.StringUtils;
import midp.tapeline.midlegram.client.data.Media;
import midp.tapeline.midlegram.client.data.Message;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.forms.AudioMediaForm;
import midp.tapeline.midlegram.ui.forms.ChatForm;
import midp.tapeline.midlegram.ui.forms.ImageMediaForm;
import midp.tapeline.midlegram.ui.forms.VideoMediaForm;

public class MessageItem extends StringItem implements ItemCommandListener {

    Message message;
    ChatForm form;
    Command viewMedia;
    Command reply;
    Command goToRe;

    public MessageItem(Message message, ChatForm form) {
        super(message.authorName, message.text + "\n" +
                StringUtils.dateToString(new Date(((long) message.time) * 1000)), Item.PLAIN);
        this.form = form;
        this.message = message;
        setLayout(Item.LAYOUT_EXPAND);
        setItemCommandListener(this);
        reply = new Command("Reply", Command.SCREEN, 1);
        goToRe = new Command("Navigate to preceding", Command.SCREEN, 1);
        addCommand(reply);
        if (message.inReplyTo != null)
            addCommand(goToRe);
        if (message.media.size() != 0) {
            viewMedia = new Command("View media", Command.SCREEN, 1);
            addCommand(viewMedia);
            setDefaultCommand(viewMedia);
        }
    }

    public void commandAction(Command cmd, Item arg1) {
        if (cmd == viewMedia) {
            Media m = (Media) message.media.elementAt(0);
            if (m.mimetype.startsWith("image"))
                UI.startForm(new ImageMediaForm(m));
            else if (m.mimetype.startsWith("audio"))
                UI.startForm(new AudioMediaForm(m));
            else if (m.mimetype.startsWith("video"))
                UI.startForm(new VideoMediaForm(m));
        } else if (cmd == reply) {
            form.setReplyTo(message);
        } else if (cmd == goToRe) {
            form.goToMsg(message.inReplyTo.longValue());
        }
    }

}
