package midp.tapeline.midlegram.ui.forms;

import midp.tapeline.midlegram.G;
import midp.tapeline.midlegram.database.vls.VLSEngine;
import midp.tapeline.midlegram.filesystem.FS;
import midp.tapeline.midlegram.uibase.FormActivity;

import javax.microedition.lcdui.*;
import java.io.IOException;

public class DebugVLSForm extends FormActivity {

    public DebugVLSForm() {
        super("Debug VLS");
        VLSEngine vls = null;
        try {
            vls = new VLSEngine("file:///E:/vlstest/");
            FS.ensureDirExists("file:///E:/vlstest/");
            vls.open();
            long gid = vls.append("Hello2".getBytes());
            System.out.println("Placed with gid " + gid);
            byte[] data = vls.maybeGet(gid);
            if (data == null)
                System.err.println("data is null");
            else {
                System.out.println(new String(data));
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (vls != null) vls.close();
        }
        addBackButton();
    }

}
