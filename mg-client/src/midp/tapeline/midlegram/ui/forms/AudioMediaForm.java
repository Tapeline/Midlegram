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
import java.io.IOException;

public class AudioMediaForm extends UIForm implements PlayerListener {

    Media media;
    Player player;
    String filename;
    Command playPause = new Command("Play/pause", Command.SCREEN, 1);
    StringItem playPauseButton;
    StringItem playerInfo;
    Gauge playerGauge;
    PlayerTimeWatcher playerWatcher;
    boolean isPaused = true;

    public AudioMediaForm(Media media) {
        super("Media audio");
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
            throw new RuntimeException(e.toString());
        }
    }

    private void render() {
        deleteAll();
        try {
            player = Manager.createPlayer(filename);
            player.addPlayerListener(this);
            player.realize();
            player.prefetch();
            playPauseButton = new StringItem("", "Play", StringItem.BUTTON);
            playPauseButton.setDefaultCommand(playPause);
            playPauseButton.setItemCommandListener(this);
            playPauseButton.setLayout(Item.LAYOUT_CENTER);
            playerGauge = new Gauge(null, false, (int) (player.getDuration() / 1000000), 0);
            playerInfo = new StringItem(
                    "Now playing",
                    "0:00 / " + StringUtils.toMMSS((int) (player.getDuration() / 1000000))
            );
            playerWatcher = new PlayerTimeWatcher(playerGauge, player, playerInfo);
            append(playerInfo);
            append(playerGauge);
            append(playPauseButton);
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
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

}
