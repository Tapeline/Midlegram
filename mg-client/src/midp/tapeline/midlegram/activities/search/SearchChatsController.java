package midp.tapeline.midlegram.activities.search;

import midp.tapeline.midlegram.G;
import midp.tapeline.midlegram.client.requests.ClientRunSearch;
import midp.tapeline.midlegram.client.responses.ClientSearchPerformed;
import midp.tapeline.midlegram.events.Message;
import midp.tapeline.midlegram.events.MessageBus;
import midp.tapeline.midlegram.events.Subscriber;

public class SearchChatsController implements Subscriber {

    SearchChatsForm form;
    long requestId = -1;

    public SearchChatsController(SearchChatsForm form) {
        this.form = form;
    }

    public void init() {
        G.messageBus.subscribe(ClientSearchPerformed.class, this);
    }

    public void deinit() {
        G.messageBus.unsubscribeAll(this);
    }

    public void search(String query) {
        requestId = G.messageBus.nextSeq();
        G.messageBus.publish(new ClientRunSearch(requestId, query));
    }

    public void onReceive(MessageBus bus, Message message) {
        if (message instanceof ClientSearchPerformed && message.seq == requestId) {
            form.updateResults(((ClientSearchPerformed) message).chats);
        }
    }

}
