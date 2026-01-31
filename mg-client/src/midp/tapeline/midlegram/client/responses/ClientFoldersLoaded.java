package midp.tapeline.midlegram.client.responses;

import midp.tapeline.midlegram.events.Message;

import java.util.Vector;

public class ClientFoldersLoaded extends Message {

    public final Vector folders;

    public ClientFoldersLoaded(long seq, Vector folders) {
        super(seq);
        this.folders = folders;
    }

}
