package gui.elements;

import main.SlickInitializer;
import misc.MiscMath;
import misc.MiscString;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

import java.util.ArrayList;
import java.util.Random;

public class TextDisplay extends GUIElement {
    double alpha = 1;
    String text = "", title = "";
    boolean _float = false;
    
    ArrayList<String> lines = new ArrayList<String>();
    
    float render_offset_y = 0, x_vel = 0, y_vel = 0, render_offset_x = 0;
    
    Color background, title_color, text_color;
    boolean show_shadow, show_background;
    
    /**
     * Creates a new TextDisplay.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param width The width of the display (text will wrap).
     */
    public TextDisplay(int width) {
        this.setWidth(width);
        this.text = "";
        this.text_color = Color.black;
        this.background = Color.white;
        this.title_color = Color.white;
    }
    
    public String getText() {
        return text;
    }
    
    /**
     * Animates the TextDisplay. Moves it in a direction.
     * @param dx Should the TextDisplay randomize it's x velocity?
     * @param dy The y velocity multiplier.
     */
    public void fly(boolean dx, float dy) {
        _float = true;
        y_vel = dy;
        if (dx) {
            x_vel = (new Random().nextInt() % 125);
        } else {
            x_vel = 0;
        }
    }
    
    public void setBackgroundColor(Color c) {
        background = c;
    }
    
    public void setTitle(String t) {
        title = t;
        if (title == null) {
            title = "";
        }
    }
    
    public void setTitleColor(Color c) {
        title_color = c;
    }
    
    public void setTextColor(Color c) {
        text_color = c;
    }
    
    public void addLine(String t) {
        lines.add(t);
    }
    
    public void addText(String t) {
        text+=t;
    }
    
    public void showBackground(boolean s) {
        show_background = s;
    }
    
    public void clearText() {
        text = "";
        lines.clear();
    }
    
    public void draw(Graphics g) {
        
        if (_float) {
            alpha -= MiscMath.getConstant(Math.abs(y_vel), 1);
            setY(getY()+MiscMath.getConstant(100*y_vel*alpha, 1));
            render_offset_x += MiscMath.getConstant(x_vel*alpha, 1);
        }
        
        if (alpha <= 0) { hide(); return; }
        if (text == null) return;
        if (text.equals("") && lines.isEmpty()) return;
        
        if (title.length() == 0) {
            String[] rows = MiscString.wrap(text, getWidth(), SlickInitializer.NORMAL_FONT);
            if (!lines.isEmpty()) rows = lines.toArray(new String[0]);
            setHeight((rows.length*20) + 8);
            if (!_float) {
                g.setColor(background);
                g.fillRect((int)getX(), (int)getY(), getWidth(), getHeight());
                g.setColor(background.darker().darker());
                g.drawRect((int)getX(), (int)getY(), getWidth(), getHeight());
            }
            g.setFont(SlickInitializer.NORMAL_FONT);
            int str_h = 0;
            for (int i = 0; i != rows.length; i++) {
                String r = rows[i]+" ";
                g.setColor(Color.darkGray);
                g.drawString(r, (int)getX()+6+(int)render_offset_x, (int)getY()+str_h+6+(int)render_offset_y);
                g.setColor(text_color);
                g.drawString(r, (int)getX()+5+(int)render_offset_x, (int)getY()+str_h +5+(int)render_offset_y);
                str_h+=20;
            }
        } else {
            String[] rows = MiscString.wrap(text, getWidth(), SlickInitializer.SMALL_FONT);
            if (!lines.isEmpty()) rows = lines.toArray(new String[0]);
            setHeight((rows.length*20) + 30);
            if (!_float) {
                g.setColor(background);
                g.fillRect((int)getX(), (int)getY(), getWidth(), getHeight());
                g.setColor(background.darker().darker());
                g.drawRect((int)getX(), (int)getY(), getWidth(), getHeight());
            }
            g.setFont(SlickInitializer.NORMAL_FONT);
            g.setColor(Color.darkGray);
            g.drawString(title, (int)getX()+6+(int)render_offset_x, (int)getY()+6+(int)render_offset_y);
            g.setColor(title_color);
            g.drawString(title, (int)getX()+5+(int)render_offset_x, (int)getY()+5+(int)render_offset_y);
            g.setFont(SlickInitializer.SMALL_FONT);
            
            int str_h = 25;
            for (int i = 0; i != rows.length; i++) {
                String r = rows[i]+" ";
                g.setColor(Color.darkGray);
                g.drawString(r, (int)getX()+6+(int)render_offset_x, (int)getY()+str_h+1+(int)render_offset_y);
                g.setColor(text_color);
                g.drawString(r, (int)getX()+5+(int)render_offset_x, (int)getY()+str_h+(int)render_offset_y);
                str_h+=20;
            }
        }

    }
    
}
