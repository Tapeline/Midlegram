package midp.tapeline.midlegram.activities.chat;

import midp.tapeline.midlegram.state.data.Chat;
import midp.tapeline.midlegram.ui.base.FormActivity;

public class ChatForm extends FormActivity {

    private Chat chat;

    public ChatForm(Chat chat) {
        super(chat.title);
        append("Nothing here yet");
        addBackButton();
    }

}
