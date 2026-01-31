package midp.tapeline.midlegram.client;

import midp.tapeline.midlegram.client.requests.ClientLoadChats;
import midp.tapeline.midlegram.client.requests.ClientLoadFolders;
import midp.tapeline.midlegram.client.requests.ClientRunSearch;
import midp.tapeline.midlegram.client.responses.ClientChatsLoaded;
import midp.tapeline.midlegram.client.responses.ClientFoldersLoaded;
import midp.tapeline.midlegram.client.responses.ClientRequestFailed;
import midp.tapeline.midlegram.client.responses.ClientSearchPerformed;
import midp.tapeline.midlegram.events.Message;
import midp.tapeline.midlegram.events.MessageBus;
import midp.tapeline.midlegram.events.Subscriber;
import midp.tapeline.midlegram.state.data.PaginatedChats;

import java.io.IOException;
import java.util.Vector;

public class AsyncClient implements Subscriber {

    private TelegramClient client;

    public AsyncClient(TelegramClient client) {
        this.client = client;
    }

    public void subscribe(MessageBus bus) {
        bus.subscribe(ClientLoadFolders.class, this);
        bus.subscribe(ClientLoadChats.class, this);
        bus.subscribe(ClientRunSearch.class, this);
    }

    public void onReceive(MessageBus bus, Message message) {
        try {
            handle(bus, message);
        } catch (IOException e) {
            bus.publish(new ClientRequestFailed(message.seq, e.toString()));
        }
    }

    private void handle(MessageBus bus, Message message) throws IOException {
        if (message instanceof ClientLoadFolders) {
            Vector folders = client.getFolders();
            bus.publish(new ClientFoldersLoaded(message.seq, folders));
        } else if (message instanceof ClientLoadChats) {
            ClientLoadChats request = (ClientLoadChats) message;
            PaginatedChats response = client.getChats(
                request.folderId, request.page);
            bus.publish(new ClientChatsLoaded(request.seq, response.chats, response.totalPages));
        } else if (message instanceof ClientRunSearch) {
            ClientRunSearch request = (ClientRunSearch) message;
            bus.publish(new ClientSearchPerformed(request.seq, client.searchChats(request.query, 1000)));
        }
    }

}
