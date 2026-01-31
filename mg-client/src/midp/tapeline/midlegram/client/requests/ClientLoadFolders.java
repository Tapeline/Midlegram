package midp.tapeline.midlegram.client.requests;

import midp.tapeline.midlegram.events.Message;

public class ClientLoadFolders extends Message {

    public ClientLoadFolders(long seq) {
        super(seq);
    }

}
