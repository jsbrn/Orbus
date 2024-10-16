package gui.elements;

import misc.MiscMath;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class Notification extends GUIElement {
    
    boolean dropping = false, moving = false;
    int duration = 0;
    double timer = 0;
    TextDisplay td;
    
    /**
     * Create a Notification element that tracks whether it is dropping down or not, and
     * allows you to set the text. You must handle the actual dropping down or other motion separately.
     * This is to allow customization.
     */
    public Notification(int width) {
        td = new TextDisplay(width);
        td.setBackgroundColor(new Color(235, 230, 190, 100));
        td.setTitleColor(Color.yellow);
        td.setTextColor(Color.white);
    }
    
    public void setDuration(int d) {
        duration = d;
    }
    
    public void setText(String s) {
        td.clearText();
        td.addText(s);
    }
    
    public void setTitle(String s) {
        td.setTitle(s);
    }
    
    public void resetTimer() {
        timer = duration;
    }
    
    public boolean isDropping() {
        return dropping;
    }
    
    public void setDropping(boolean d) {
        dropping = d;
    }
    
    public void draw(Graphics g) {
        if (td.getText() == null) return;
        setWidth(td.getWidth());
        setHeight(td.getHeight());
        timer -= MiscMath.getConstant(1, 1);
        if (timer <= 0) {
            dropping = false;
        }
        td.setX(getX());
        td.setY(getY());
        td.draw(g);
    }

}
