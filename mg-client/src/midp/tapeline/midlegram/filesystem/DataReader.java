package midp.tapeline.midlegram.filesystem;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class DataReader implements BinaryReader {

    protected abstract Object readData(DataInputStream dis) throws IOException;

    public Object read(InputStream is) throws IOException {
        DataInputStream dis = new DataInputStream(is);
        try {
            return readData(dis);
        } finally {
            dis.close();
        }
    }

}
