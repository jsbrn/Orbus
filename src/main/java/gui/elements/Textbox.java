package gui.elements;

import main.SlickInitializer;
import misc.MiscMath;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class Textbox extends GUIElement {
    
    String text = "", text_empty = "";
    Color color, text_empty_color;
    int maxTextWidth = 0;
    boolean show_background = true, cursor_visible = true;
    double cursor_blink_timer = 1;
    
    /**
     * Creates a new Textbox at (x, y) with width w and height h. Note that h specified the number of rows.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param w The width of the textbox.
     * @param h The height of the textbox (in rows).
     */
    public Textbox(int w, int h) {
        this.setWidth(w);
        this.setHeight(h); //height is number of rows
        this.maxTextWidth = w-20; //width minus 20
        this.text_empty_color = Color.white;
        this.text_empty = "";
        this.hidden = false;
        this.color = Color.black;
    }
    
    public void showBackground(boolean b) {
        show_background = b;
    }
    
    //sets text to display when there is nothing in the field
    public void setEmptyText(String s, Color c) {
        text_empty_color = c;
        text_empty = s;
    }
    
    public void show() {
        hidden = false;
    }
    
    public void hide() {
        hidden = true;
    }
    
    public void addText(String c) {
        if (this.isEnabled() && SlickInitializer.NORMAL_FONT.getWidth(text) < maxTextWidth) {
            text+=c;
        }
    }
    
    public void backspace() {
        if (text.length() > 0) {
            text = text.substring(0, text.length() - 1);
        }
    }
    
    public String getText() {
        return text;
    }
    
    /**
     * Sets the height of this Textbox.
     * @param h The new height in rows.
     */
    public void setHeight(int h) {
        H = h * 25;
    }
    
    public void clear() {
        text = "";
    }
    
    public void setTextColor(Color c) {
        color = c;
    }
    
    public void draw(Graphics g) {
        cursor_blink_timer -= MiscMath.getConstant(2, 1);
        if (cursor_blink_timer <= 0) {
            cursor_blink_timer = 1;
            cursor_visible = !cursor_visible;
        }
        if (hidden == false) {
            if (show_background) {
                g.setColor(Color.gray.darker().darker());
                g.fillRoundRect((float)X+3, (float)Y+3, W, H, 3);
                g.setColor(Color.white);
                g.fillRoundRect((float)X, (float)Y, W, H, 3);
                g.setColor(Color.black);
                g.setLineWidth(1);
                g.drawRoundRect((float)X, (float)Y, W, H, 3);
            }

            g.setFont(SlickInitializer.NORMAL_FONT);
            if (text.length() > 0) {
                g.setColor(color);
                g.drawString(text, (float)X + 3, (float)Y+4);
            } else {
                //if (this.enabled == false) {
                    g.setColor(text_empty_color.brighter());
                    g.drawString(text_empty, (float)X + 5, (float)Y + 4);
                //}
            }
            
            if (this.isEnabled() == false) {
                g.setColor(new Color(50, 50, 50, 100));
                g.fillRoundRect((float)X, (float)Y, W, H, 3);
            } else {
                if (cursor_visible) {
                    g.setColor(Color.black);
                    g.setLineWidth(2);
                    g.drawLine((int)X+3+(SlickInitializer.NORMAL_FONT.getWidth(text)), (int)Y+3, 
                            (int)X+3+(SlickInitializer.NORMAL_FONT.getWidth(text)), (int)Y+22);
                    g.setLineWidth(1);
                }
            }
        }
    }
    
}