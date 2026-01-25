package midp.tapeline.midlegram.filesystem;

import java.io.IOException;
import java.io.InputStream;

public interface BinaryReader {

    Object read(InputStream is) throws IOException;

}
