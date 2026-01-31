package midp.tapeline.midlegram.activities.folderlist;

import midp.tapeline.midlegram.G;
import midp.tapeline.midlegram.client.requests.ClientLoadFolders;
import midp.tapeline.midlegram.client.responses.ClientFoldersLoaded;
import midp.tapeline.midlegram.events.Message;
import midp.tapeline.midlegram.events.MessageBus;
import midp.tapeline.midlegram.events.Subscriber;

import java.util.Vector;

public class FolderListController implements Subscriber {

    FolderListForm form;
    long requestSeq = -1;

    public FolderListController(FolderListForm form) {
        this.form = form;
    }

    public void init() {
        G.messageBus.subscribe(ClientFoldersLoaded.class, this);
    }

    public void deinit() {
        G.messageBus.unsubscribeAll(this);
    }

    public Vector maybeGetFolders() {
        Vector maybeFolders = G.store.maybeGetFolders();
        if (maybeFolders == null) {
            requestSeq = G.messageBus.nextSeq();
            G.messageBus.publish(new ClientLoadFolders(requestSeq));
        }
        return maybeFolders;
    }

    public void onReceive(MessageBus bus, Message message) {
        if (message instanceof ClientFoldersLoaded && message.seq == requestSeq) {
            G.store.setFolders(((ClientFoldersLoaded) message).folders);
            form.updateFolders(((ClientFoldersLoaded) message).folders);
        }
    }

}
