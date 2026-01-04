package midp.tapeline.midlegram.client.data;

public class AttachedMedia {

    public final String type;
    public final byte[] data;
    public final String file;

    public AttachedMedia(String type, byte[] data) {
        this.type = type;
        this.data = data;
        this.file = null;
    }

    public AttachedMedia(String type, String path) {
        this.type = type;
        this.data = null;
        this.file = path;
    }

}
