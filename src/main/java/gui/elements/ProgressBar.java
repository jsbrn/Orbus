package gui.elements;

import main.SlickInitializer;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ProgressBar extends GUIElement {

    double max, progress;
    String title;

    public ProgressBar(int w, int h, String tit) {
        this.setWidth(w);
        this.setHeight(h);
        this.max = 0;
        this.title = tit;
    }

    public void setProgress(double p) {
        progress = p;
    }

    public void addProgress(double p) {
        progress += p;
    }

    public double getProgress() {
        return progress;
    }

    public double getMaxProgress() {
        return progress;
    }

    public void reset() {
        progress = 0;
    }

    public void setMaxProgress(double m) {
        max = m;
    }

    public void setTitle(String t) {
        title = t;
    }

    public void setAsComplete() {
        progress = max+1;
    }

    public boolean isComplete() {
        return progress >= max && hidden == false && max > 0;
    }
    
    public int getProgressPercent() {
        return (int)((progress/max)*100);
    }

    public void draw(Graphics g) {
        if (hidden == false) {
            g.setFont(SlickInitializer.NORMAL_FONT);
            g.setColor(Color.white.darker().darker());
            g.drawString(title, (float)X + (W / 2) - (SlickInitializer.NORMAL_FONT.getWidth(title) / 2)+1, (float)Y - 22);
            g.setColor(Color.white);
            g.drawString(title, (float)X + (W / 2) - (SlickInitializer.NORMAL_FONT.getWidth(title) / 2), (float)Y - 23);
            g.setColor(Color.gray);
            g.fillRect((float)X, (float)Y, W, H);

            float length = (float) (progress * (W / max));
            g.setColor(Color.green);
            g.fillRect((float)X, (float)Y, length, H);
        }
    }

}
