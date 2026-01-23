package midp.tapeline.midlegram.filesystem;

import java.io.IOException;
import java.io.InputStream;

public interface DataReader {

    Object read(InputStream is) throws IOException;

}
