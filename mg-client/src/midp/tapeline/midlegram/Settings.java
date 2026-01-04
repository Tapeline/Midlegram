package midp.tapeline.midlegram;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotFoundException;

public class Settings {

    public static String sessionKey = null;

    public static void load() {
        try {
            RecordStore r = RecordStore.openRecordStore("midlegram_prefs", true);
            if (r.getNumRecords() == 0) {
                r.addRecord(new byte[]{0}, 0, 1);
            }
            byte[] key = r.getRecord(1);
            if (key.length != 36) {
                sessionKey = null;
            } else {
                sessionKey = new String(key);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }

    public static void save() {
        try {
            RecordStore r = RecordStore.openRecordStore("midlegram_prefs", true);
            if (sessionKey == null) {
                r.setRecord(1, new byte[]{0}, 0, 1);
            } else {
                r.setRecord(1, sessionKey.getBytes(), 0, 36);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
    }


}
