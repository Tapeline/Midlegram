package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

import midp.tapeline.midlegram.Services;
import midp.tapeline.midlegram.client.data.ChatFolder;
import midp.tapeline.midlegram.client.data.Media;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;
import midp.tapeline.midlegram.ui.components.ChatFolderItem;

public class MediaForm extends UIForm {
	
	Media media;
	byte[] content;
	
	public MediaForm(Media media) {
		super("Media");
		this.media = media;
		addBackButton();
	}
	
	public void onStart() {
		setLoading(true);
		try {
			content = Services.tg.getFile(media.id, media.mimetype);
			render();
		} catch (IOException exc) {
			UI.alertFatal(exc);
		} finally {
			setLoading(false);
		}
	}
	
	private void render() {
		deleteAll();
		if (media.mimetype.startsWith("image")) {
			try {
				Image img = Image.createImage(content, 0, content.length);
		        ImageItem imageitem = new ImageItem("", img, ImageItem.LAYOUT_CENTER, "");
		        append(imageitem);
			} catch (Exception e) {
				UI.alertFatal(e);
			}
		} else if (media.mimetype.startsWith("audioaaaa")) {
			
		} else {
			UI.alertInfo("Unknown media type " + media.mimetype);
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
		}
	}
	
	public void onResume() {
		setLoading(false);
	}
	
}
