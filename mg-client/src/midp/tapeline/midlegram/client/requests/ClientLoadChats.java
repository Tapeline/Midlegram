package midp.tapeline.midlegram.client.requests;

import midp.tapeline.midlegram.events.Message;

public class ClientLoadChats extends Message {

    public final long folderId;
    public final int page;

    public ClientLoadChats(long seq, long folderId, int page) {
        super(seq);
        this.folderId = folderId;
        this.page = page;
    }

}
