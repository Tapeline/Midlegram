package midp.tapeline.midlegram.ui.forms;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;

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
		Image img = Image.createImage(content, 0, content.length);
        ImageItem imageitem = new ImageItem("", img, ImageItem.LAYOUT_CENTER, "");
        append(imageitem);
	}
	
	public void onResume() {
		setLoading(false);
	}
	
}
