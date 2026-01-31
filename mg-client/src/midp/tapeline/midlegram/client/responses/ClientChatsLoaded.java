package midp.tapeline.midlegram.client.responses;

import midp.tapeline.midlegram.events.Message;

import java.util.Vector;

public class ClientChatsLoaded extends Message {

    public final Vector chats;
    public final int totalPages;

    public ClientChatsLoaded(long seq, Vector chats, int totalPages) {
        super(seq);
        this.chats = chats;
        this.totalPages = totalPages;
    }

}
