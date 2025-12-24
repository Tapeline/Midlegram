package midp.tapeline.midlegram.client.data;

public class Media {

	public final String mimetype;
	public final int id;
	public final long size;
	
	public Media(String mimetype, int id, long size) {
		this.mimetype = mimetype;
		this.id = id;
		this.size = size;
	}
	
}
