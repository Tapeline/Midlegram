package midp.tapeline.midlegram.client.data;

public class Chat {

    public final long id;
    public final String title;
    public int unreadCount;
    public String lastMessage;  // Nullable

    public Chat(long id, String title, int unreadCount, String lastMessage) {
        super();
        this.id = id;
        this.title = title;
        this.unreadCount = unreadCount;
        this.lastMessage = lastMessage;
    }

}
