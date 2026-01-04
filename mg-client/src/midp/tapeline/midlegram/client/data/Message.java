package midp.tapeline.midlegram.client.data;

import java.util.Vector;

public class Message {

    public static final byte TEXT = 0;
    public static final byte PHOTO = 1;
    public static final byte VIDEO = 2;
    public static final byte VOICE = 3;
    public static final byte UNKNOWN = 4;

    public final long id;
    public final byte type;
    public final int time;
    public final long author_id;
    public final String text;
    public final String authorName;
    public final String authorHandle;
    public final Vector media;
    public final Long inReplyTo;

    public Message(long id, byte type, int time, long author_id, String text) {
        this(id, type, time, author_id, text, "User " + id, "?", new Vector(), null);
    }

    public Message(long id, byte type, int time, long author_id,
                   String text, String authorName, String authorHandle, Vector media, Long inReplyTo) {
        super();
        this.id = id;
        this.type = type;
        this.time = time;
        this.author_id = author_id;
        this.text = text;
        this.authorHandle = authorHandle;
        this.authorName = authorName;
        this.media = media;
        this.inReplyTo = inReplyTo;
    }

    public String toString() {
        return text;
    }

}
