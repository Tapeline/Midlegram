package midp.tapeline.midlegram.state.data;

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
    public final long authorId;
    public final String authorName;
    public final String authorHandle;
    public final String text;
    public final Vector media;
    public final Long maybeInReplyTo;

    public Message(
        long id,
        byte type,
        int time,
        long authorId,
        String authorName,
        String authorHandle,
        String text,
        Vector media,
        Long maybeInReplyTo
    ) {
        this.id = id;
        this.type = type;
        this.time = time;
        this.authorId = authorId;
        this.text = text;
        this.authorName = authorName;
        this.authorHandle = authorHandle;
        this.media = media;
        this.maybeInReplyTo = maybeInReplyTo;
    }

    public String toString() {
        return text;
    }

    public boolean isReply() {
        return maybeInReplyTo != null;
    }

    public long getInReplyTo() {
        return maybeInReplyTo.longValue();
    }

}
