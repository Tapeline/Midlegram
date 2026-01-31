package midp.tapeline.midlegram.state;

import midp.tapeline.midlegram.state.data.PaginatedChats;

import java.util.Vector;

public class TelegramStore {

    Vector folders = null;
    PaginatedChats chats = null;

    public Vector maybeGetFolders() {
        return folders;
    }

    public void setFolders(Vector folders) {
        this.folders = folders;
    }

    public PaginatedChats maybeGetChats(long folderId, int page) {
        return null;
    }

    public void setChats(PaginatedChats chats) {
        //this.chats = chats;
    }

}
