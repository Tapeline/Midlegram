package midp.tapeline.midlegram.state.data;

import java.util.Vector;

public class PaginatedChats {

    public final Vector chats;
    public final int totalPages;

    public PaginatedChats(Vector chats, int totalPages) {
        this.chats = chats;
        this.totalPages = totalPages;
    }

}
