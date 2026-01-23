package midp.tapeline.midlegram.state.data;

import java.util.Vector;

public class ChatFolder {

    public final long id;
    public final String name;
    public final Vector chatIds;

    public ChatFolder(long id, String name, Vector chatIds) {
        this.id = id;
        this.name = name;
        this.chatIds = chatIds;
    }

}
