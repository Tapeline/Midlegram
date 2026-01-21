package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VideoControl;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.StringUtils;
import midp.tapeline.midlegram.client.data.Media;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UICanvas;
import midp.tapeline.midlegram.ui.UIForm;

public class VideoMediaForm extends UICanvas implements PlayerListener {

    Media media;
    Player player;
    String filename;

    public VideoMediaForm(Media media) {
        super();
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
        try {
            //ByteArrayInputStream bais = new ByteArrayInputStream(content);
            player = Manager.createPlayer(filename);
            player.addPlayerListener(this);
            player.realize();
            player.prefetch();
            VideoControl videoControl = (VideoControl) (player.getControl("VideoControl"));
            if (videoControl == null) {
                UI.alertFatal("VideoControl not supported");
            } else {
                videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
                videoControl.setVisible(true);
            }
        } catch (Exception e) {
            UI.alertFatal(e);
        }
    }

    public void onResume() {
        setLoading(false);
    }

    public void playerUpdate(Player p, String event, Object eventData) {
        if (event.equals(PlayerListener.END_OF_MEDIA)) {
            try {
                p.stop();
            } catch (MediaException e) {
                e.printStackTrace();
            }
        }
    }

}
