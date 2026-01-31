package midp.tapeline.midlegram.client.responses;

import midp.tapeline.midlegram.events.Message;

import java.util.Vector;

public class ClientSearchPerformed extends Message {

    public final Vector chats;

    public ClientSearchPerformed(long seq, Vector chats) {
        super(seq);
        this.chats = chats;
    }

}
