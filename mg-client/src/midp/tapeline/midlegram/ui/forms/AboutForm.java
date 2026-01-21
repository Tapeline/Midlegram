package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.Settings;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;

public class AboutForm extends UIForm {

    public AboutForm() {
        super("About");
        Image image;
        try {
            image = Image.createImage("/icon.png");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
        ImageItem imageitem = new ImageItem("Midlegram v1.4", image, ImageItem.LAYOUT_CENTER, "");
        append(imageitem);
        append(new StringItem("Author", "Tapeline"));
        append(new StringItem("Debug", ""));
        String apiVersion = System.getProperty("microedition.media.version");
        append("MM API version:" + apiVersion + "\n");
        append("Mixing supported: " + System.getProperty("supports.mixing") + "\n");
        append("Audio capture supported: " + System.getProperty("supports.audio.capture") + "\n");
        append("Video capture supported: " + System.getProperty("supports.video.capture") + "\n");
        append("Recording supported: " + System.getProperty("supports.recording") + "\n");
        append("Supported audio encodings: " + System.getProperty("audio.encodings") + "\n");
        append("Supported video encodings: " + System.getProperty("video.encodings") + "\n");
        append("Supported video snaphot encodings: " + System.getProperty("video.snapshot.encodings") + "\n");
        append("\n");
        addBackButton();
    }

}
