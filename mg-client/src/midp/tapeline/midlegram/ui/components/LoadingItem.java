package midp.tapeline.midlegram.ui.components;

import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Item;

import midp.tapeline.midlegram.ui.Animation;
import midp.tapeline.midlegram.ui.Animation.Repaintable;

public class LoadingItem extends CustomItem implements Repaintable {

    private int progress = -1;
    private boolean isActive = false;
    private long speed = 10;

    public LoadingItem() {
        super(null);
        Animation.addAnimable(this);
        setLayout(Item.LAYOUT_EXPAND);
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public void setIndeterminate() {
        setProgress(-1);
    }

    protected int getMinContentHeight() {
        return isActive ? 8 : 0;
    }

    protected int getMinContentWidth() {
        return isActive ? 8 : 0;
    }

    protected int getPrefContentHeight(int arg0) {
        return isActive ? 8 : 0;
    }

    protected int getPrefContentWidth(int arg0) {
        return isActive ? 8 : 0;
    }

    protected void paint(Graphics g, int arg1, int arg2) {
        if (!isActive) return;
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, g.getClipWidth(), g.getClipHeight());
        if (progress == -1) {
            int x = (int) ((Animation.t * speed) % g.getClipWidth());
            //int x = animSegX(g.getClipWidth());
            g.setColor(63, 63, 255);
            g.fillRect(x, 0, x + 32, g.getClipHeight());
        } else {
            int w = g.getClipWidth() * progress / 100;
            g.setColor(63, 63, 255);
            g.fillRect(0, 0, w, g.getClipHeight());
        }
    }

    private int animSegX(int w) {
        int g = (int) ((Animation.t * speed) % w) * 1000 / w;
        int k = g + g * g / 1000;
        return g * k / 250;

    }

    public void update() {
        repaint();
    }

}
