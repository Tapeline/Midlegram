package midp.tapeline.midlegram.activities.chatlist;

import midp.tapeline.midlegram.G;
import midp.tapeline.midlegram.client.requests.ClientLoadChats;
import midp.tapeline.midlegram.client.requests.ClientLoadFolders;
import midp.tapeline.midlegram.client.responses.ClientChatsLoaded;
import midp.tapeline.midlegram.client.responses.ClientFoldersLoaded;
import midp.tapeline.midlegram.events.Message;
import midp.tapeline.midlegram.events.MessageBus;
import midp.tapeline.midlegram.events.Subscriber;
import midp.tapeline.midlegram.state.data.ChatFolder;
import midp.tapeline.midlegram.state.data.PaginatedChats;

import java.util.Vector;

public class ChatListController implements Subscriber {

    ChatListForm form;
    ChatFolder folder;
    long requestSeq = -1;

    public ChatListController(ChatListForm form, ChatFolder folder) {
        this.form = form;
        this.folder = folder;
    }

    public void init() {
        G.messageBus.subscribe(ClientChatsLoaded.class, this);
    }

    public void deinit() {
        G.messageBus.unsubscribeAll(this);
    }

    public PaginatedChats maybeGetChats(int page) {
        PaginatedChats maybeChats = G.store.maybeGetChats(folder.id, page);
        if (maybeChats == null) {
            requestSeq = G.messageBus.nextSeq();
            G.messageBus.publish(new ClientLoadChats(
                requestSeq, folder.id, page
            ));
        }
        return maybeChats;
    }

    public void onReceive(MessageBus bus, Message message) {
        if (message instanceof ClientChatsLoaded && message.seq == requestSeq) {
            ClientChatsLoaded response = (ClientChatsLoaded) message;
            PaginatedChats chats = new PaginatedChats(response.chats, response.totalPages);
            G.store.setChats(chats);
            form.updateChats(chats);
        }
    }

}
