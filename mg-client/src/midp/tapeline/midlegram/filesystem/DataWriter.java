package midp.tapeline.midlegram.filesystem;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class DataWriter implements BinaryWriter {

    protected abstract void writeData(DataOutputStream dos) throws IOException;

    public void write(OutputStream os) throws IOException {
        DataOutputStream dos = new DataOutputStream(os);
        try {
            writeData(dos);
        } finally {
            dos.close();
        }
    }

}
