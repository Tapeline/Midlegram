package midp.tapeline.midlegram.activities.about;

import midp.tapeline.midlegram.ui.base.FormActivity;

import java.io.IOException;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

public class AboutForm extends FormActivity {

    public AboutForm() {
        super("About Midlegram");
        Image image;
        try {
            image = Image.createImage("/icon.png");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.toString());
        }
        ImageItem imageitem = new ImageItem(
            "Midlegram v2.0", image, ImageItem.LAYOUT_CENTER, ""
        );
        append(imageitem);
        StringItem copy = new StringItem("Copyright", "(c) 2025-2026 Tapeline");
        copy.setLayout(Item.LAYOUT_LEFT | ImageItem.LAYOUT_EXPAND);
        append(copy);
        StringItem debug = new StringItem("Debug info", "");
        debug.setLayout(Item.LAYOUT_LEFT | ImageItem.LAYOUT_EXPAND);
        append(debug);
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
