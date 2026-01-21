package midp.tapeline.midlegram.ui.forms;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.StringUtils;
import midp.tapeline.midlegram.client.data.Media;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VideoControl;
import java.io.IOException;

public class ImageMediaForm extends UIForm {

    Media media;
    String filename;

    public ImageMediaForm(Media media) {
        super("Media image");
        this.media = media;
        addBackButton();
    }

    public void onStart() {
        setLoading(true);
        try {
            filename = Services.tg.getFileToFile(media.id, media.mimetype);
            render();
        } catch (IOException exc) {
            UI.alertFatal(exc);
        } finally {
            setLoading(false);
        }
    }

    public void onEnd() {
        try {
            FileConnection fc = (FileConnection) Connector.open(filename, Connector.WRITE);
            fc.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void render() {
        deleteAll();
        try {
            Image img = Image.createImage(filename);
            ImageItem imageitem = new ImageItem(
                    null, img, ImageItem.LAYOUT_EXPAND | ImageItem.LAYOUT_VEXPAND, null
            );
            append(imageitem);
        } catch (Exception e) {
            UI.alertFatal(e);
        }
    }

    public void onResume() {
        setLoading(false);
    }

}
