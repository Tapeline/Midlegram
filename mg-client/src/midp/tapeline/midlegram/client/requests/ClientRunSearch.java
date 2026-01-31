package midp.tapeline.midlegram.client.requests;

import midp.tapeline.midlegram.events.Message;

public class ClientRunSearch extends Message {

    public final String query;

    public ClientRunSearch(long seq, String query) {
        super(seq);
        this.query = query;
    }

}
