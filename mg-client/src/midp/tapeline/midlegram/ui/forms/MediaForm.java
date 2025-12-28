package midp.tapeline.midlegram.ui.forms;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Vector;

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
import midp.tapeline.midlegram.client.data.ChatFolder;
import midp.tapeline.midlegram.client.data.Media;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;
import midp.tapeline.midlegram.ui.components.ChatFolderItem;

public class MediaForm extends UIForm implements PlayerListener {
	
	Media media;
	Player player;
	byte[] content;
	Command playPause = new Command("Play/pause", Command.SCREEN, 1);
	StringItem playPauseButton;
	StringItem playerInfo;
	Gauge playerGauge;
	PlayerTimeWatcher playerWatcher;
	boolean isPaused = true;
	
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
		} else if (media.mimetype.startsWith("audio")) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(content);
				player = Manager.createPlayer(bais, "audio/aac");
				player.addPlayerListener(this);
				player.realize();
	            player.prefetch();
	            playPauseButton = new StringItem("", "Play", StringItem.BUTTON);
	            playPauseButton.setDefaultCommand(playPause);
	            playPauseButton.setItemCommandListener(this);
	            playPauseButton.setLayout(Item.LAYOUT_CENTER);
	            playerGauge = new Gauge(null, false, (int) (player.getDuration() / 1000000), 0);
	            playerInfo = new StringItem(
	            		"Now playing", "0:00 / " + StringUtils.toMMSS((int) (player.getDuration() / 1000000))
        		);
	            playerWatcher = new PlayerTimeWatcher(playerGauge, player, playerInfo);
	            append(playerInfo);
	            append(playerGauge);
	            append(playPauseButton);
			} catch (Exception e) {
				UI.alertFatal(e);
			}
		} else if (media.mimetype.startsWith("video")) {
			try {
				ByteArrayInputStream bais = new ByteArrayInputStream(content);
				player = Manager.createPlayer(bais, "video/mp4");
				player.addPlayerListener(this);
				player.realize();
	            player.prefetch();
	            playPauseButton = new StringItem("", "Play", StringItem.BUTTON);
	            playPauseButton.setDefaultCommand(playPause);
	            playPauseButton.setItemCommandListener(this);
	            playPauseButton.setLayout(Item.LAYOUT_CENTER);
	            playerGauge = new Gauge(null, false, (int) (player.getDuration() / 1000000), 0);
	            playerInfo = new StringItem(
	            		"Now playing", "0:00 / " + StringUtils.toMMSS((int) (player.getDuration() / 1000000))
        		);
	            playerWatcher = new PlayerTimeWatcher(playerGauge, player, playerInfo);
	            append(playerInfo);
	            append(playerGauge);
	            append(playPauseButton);
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
		} else {
			UI.alertInfo("Unknown media type " + media.mimetype);
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
	
	protected void onCommand(Command cmd) {
		if (cmd == playPause) {
			if (isPaused) {
				try {
					playerWatcher.start();
					player.start();
					playPauseButton.setText("Pause");
					isPaused = false;
				} catch (MediaException e) {
					UI.alertFatal(e);
				}
			} else {
				try {
					playerWatcher.stop();
					player.stop();
					playPauseButton.setText("Play");
					isPaused = true;
				} catch (MediaException e) {
					UI.alertFatal(e);
				}
			}
		}
	}
	
	static class PlayerTimeWatcher implements Runnable {
		Gauge gauge;
		Player player;
		StringItem now;
		boolean isRunning = true;
		PlayerTimeWatcher(Gauge gauge, Player player, StringItem now) {
			this.gauge = gauge;
			this.player = player;
			this.now = now;
		}
		void start() {
			new Thread(this).start();
		}
		void stop() {
			isRunning = false;
		}
		public void run() {
			while (isRunning) {
				gauge.setValue((int) (player.getMediaTime() / 1000000));
				now.setText(
					StringUtils.toMMSS((int) (player.getMediaTime() / 1000000)) + " / " +
					StringUtils.toMMSS((int) (player.getDuration() / 1000000))
				);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ignored) {}
			}
		}
	}
	
}
