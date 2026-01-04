package midp.tapeline.midlegram.ui;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class Splash extends Canvas {

    private Image image;
    private static final int BG = 0x2D77B7;

    public Splash() {
        this.setFullScreenMode(true);
        try {
            image = Image.createImage("/icon.png");
        } catch (IOException e) {
            throw new RuntimeException(e.toString());
        }
    }

    protected void paint(Graphics g) {
        g.setColor(BG);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.drawImage(image, getWidth() / 2, getHeight() / 2, Graphics.VCENTER | Graphics.HCENTER);
    }

}