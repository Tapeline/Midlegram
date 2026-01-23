package midp.tapeline.midlegram.state.data;

public class Chat {

    public final long id;
    public final String title;
    public int unreadCount;
    public String lastMessage;

    public Chat(long id, String title, int unreadCount, String lastMessage) {
        this.id = id;
        this.title = title;
        this.unreadCount = unreadCount;
        this.lastMessage = lastMessage;
    }

}
