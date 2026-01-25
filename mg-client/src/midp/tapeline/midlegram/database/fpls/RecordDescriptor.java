package midp.tapeline.midlegram.database.fpls;

public final class RecordDescriptor {

    public final long gid;
    public final long offset;
    public final long length;
    public final boolean isDeleted;

    public RecordDescriptor(long gid, long offset, long length, boolean isDeleted) {
        this.gid = gid;
        this.offset = offset;
        this.length = length;
        this.isDeleted = isDeleted;
    }

}
