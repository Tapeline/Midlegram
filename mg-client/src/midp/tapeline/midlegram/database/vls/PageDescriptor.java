package midp.tapeline.midlegram.database.vls;

public final class PageDescriptor {

    public static final int UNKNOWN_ID = -1;

    public final int id;
    public final long start;
    public final long end;
    public final boolean isAvailable;

    public PageDescriptor(int id, long start, long end, boolean isAvailable) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.isAvailable = isAvailable;
    }

}
