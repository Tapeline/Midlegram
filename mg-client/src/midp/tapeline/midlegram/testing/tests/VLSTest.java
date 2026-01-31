package midp.tapeline.midlegram.testing.tests;

import midp.tapeline.midlegram.database.vls.VLSEngine;
import midp.tapeline.midlegram.filesystem.FS;
import midp.tapeline.midlegram.filesystem.FileConnector;

import java.io.IOException;

public class VLSTest {

    public static void main(String[] args) {
        FileConnector.useJse = true;
        VLSEngine vls = null;
        try {
            vls = new VLSEngine("file:///E:/vlstest/");
            FS.ensureDirExists("file:///E:/vlstest/");
            vls.open();
            long gid1 = vls.append("Hello1".getBytes());
            long gid2 = vls.append("Lorem ipsum".getBytes());
            long gid3 = vls.append("dolor sit amet".getBytes());
            byte[] data1 = vls.maybeGet(gid1);
            byte[] data2 = vls.maybeGet(gid2);
            byte[] data3 = vls.maybeGet(gid3);
            System.out.println(new String(data1));
            System.out.println(new String(data2));
            System.out.println(new String(data3));
            vls.delete(gid2);
            data2 = vls.maybeGet(gid2);
            System.out.println(data2);
            vls.inPlaceUpdate(gid1, "Recordare Jesu pie quod sum causa tuae viae".getBytes());
            data1 = vls.maybeGet(gid1);
            System.out.println(new String(data1));
            data3 = vls.maybeGet(gid3);
            System.out.println(new String(data3));
        } catch (IOException e) {
            e.printStackTrace();
            if (vls != null) vls.close();
        }
    }

}
