package midp.tapeline.midlegram.ui.forms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.StringItem;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import midp.tapeline.midlegram.Midlegram;
import midp.tapeline.midlegram.client.data.AttachedMedia;
import midp.tapeline.midlegram.ui.UI;
import midp.tapeline.midlegram.ui.UIForm;

public class RecordVoiceForm extends UIForm implements Runnable {

    private StringItem statusItem;
    private Gauge volumeGauge;
    private Command cmdRecord;
    private Command cmdStop;
    private Command cmdSend;
    private Command cmdCancel;
    private ChatForm form;

    private Player player;
    private RecordControl recordControl;
    private ByteArrayOutputStream outputStream;

    private boolean isRecording = false;
    private byte[] recordedData = null;


    public RecordVoiceForm(ChatForm form) {
        super("Voice note");
        this.form = form;
        statusItem = new StringItem("Status", "Not recording - tap to speak");
        volumeGauge = new Gauge("Activity", false, Gauge.INDEFINITE, 0);

        append(statusItem);

        cmdRecord = new Command("Start", Command.SCREEN, 1);
        cmdStop = new Command("Stop", Command.STOP, 1);
        cmdSend = new Command("Add to message", Command.OK, 1);

        addCommand(cmdRecord);
        addBackButton();
    }

    public void onCommand(Command cmd) {
        if (cmd == cmdRecord) {
            startRecording();
        } else if (cmd == cmdStop) {
            stopRecording();
        } else if (cmd == cmdSend) {
            form.addMediaToSend(new AttachedMedia("voice_note", recordedData));
            UI.endCurrent();
        }
    }

    // --- Recording Logic (Run in Thread to avoid UI freeze) ---

    private void startRecording() {
        if (isRecording) return;
        statusItem.setText("Initializing...");
        removeCommand(cmdRecord);

        // Run media setup in background
        new Thread(this).start();
    }

    public void run() {
        if (player != null)
            player.close();
        try {
            if (outputStream != null) outputStream.close();
        } catch (IOException ignored) {
        }
        try {
            // 1. Create Player for Audio Capture
            // Nokia E7 usually records AMR by default with this locator.
            // You can try "capture://audio?encoding=audio/amr" if needed.
            player = Manager.createPlayer("capture://audio");
            player.realize();

            // 2. Setup Recording Control
            recordControl = (RecordControl) player.getControl("RecordControl");
            if (recordControl == null) {
                throw new MediaException("Recording not supported");
            }

            // 3. Prepare Output Stream
            outputStream = new ByteArrayOutputStream();
            recordControl.setRecordStream(outputStream);

            // 4. Start
            recordControl.startRecord();
            player.start();

            isRecording = true;

            // Update UI from background
            updateUI(true);

        } catch (Exception e) {
            e.printStackTrace();
            isRecording = false;
            statusItem.setText("Error: " + e.getMessage());
            addCommand(cmdRecord); // Allow retry
        }
    }

    private void stopRecording() {
        if (!isRecording) return;

        try {
            // 1. Stop components
            if (recordControl != null) {
                recordControl.commit(); // Important: Flushes data to stream
            }
            if (player != null) {
                player.close(); // Releases the microphone
            }

            // 2. Get Data
            if (outputStream != null) {
                recordedData = outputStream.toByteArray();
                outputStream.close();
            }

            isRecording = false;
            updateUI(false);

        } catch (Exception e) {
            statusItem.setText("Error saving: " + e.getMessage());
        }
    }

    private void updateUI(final boolean recording) {
        // J2ME UI updates should be serial
        Display.getDisplay(Midlegram.instance).callSerially(new Runnable() {
            public void run() {
                if (recording) {
                    statusItem.setText("Recording...");
                    append(volumeGauge); // Show spinner
                    removeCommand(cmdRecord);
                    addCommand(cmdStop);
                } else {
                    statusItem.setText("Done (" + (recordedData != null ? recordedData.length : 0) + " bytes)");
                    removeCommand(cmdStop);
                    delete(volumeGauge);
                    addCommand(cmdRecord); // Allow re-record
                    addCommand(cmdSend);
                }
            }
        });
    }

    public void onEnd() {
        try {
            if (player != null) player.close();
            if (outputStream != null) outputStream.close();
        } catch (Exception e) {
        }
    }

}