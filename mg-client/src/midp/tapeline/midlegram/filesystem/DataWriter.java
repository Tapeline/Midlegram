package midp.tapeline.midlegram.filesystem;

import java.io.IOException;
import java.io.OutputStream;

public interface DataWriter {

    void write(OutputStream os) throws IOException;

}
