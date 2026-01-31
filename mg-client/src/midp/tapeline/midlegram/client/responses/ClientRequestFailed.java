package midp.tapeline.midlegram.client.responses;

import midp.tapeline.midlegram.events.Message;

public class ClientRequestFailed extends Message {

    public final String cause;

    public ClientRequestFailed(long seq, String cause) {
        super(seq);
        this.cause = cause;
    }

}
