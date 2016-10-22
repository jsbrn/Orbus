package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.main.SlickInitializer;
import java.util.ArrayList;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ListDisplay extends GUIElement {
    
    double alpha = 1;
    String title = "";
    int lines_per_page = 8;
    
    ArrayList<String> lines = new ArrayList<String>();
    
    float render_offset_y = 0, x_vel = 0, render_offset_x = 0;
    
    Color background, title_color, text_color;
    boolean show_shadow, show_background;
    
    /**
     * Creates a new TextDisplay.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @param width The width of the display (text will wrap).
     */
    public ListDisplay(int width) {
        this.setWidth(width);
        this.text_color = Color.black;
        this.background = Color.white;
        this.title_color = Color.white;
        this.lines_per_page = 8;
    }
    
    public void setLinesPerPage(int l) {
        lines_per_page = l;
    }
    
    public int linesPerPage() {
        return lines_per_page;
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
    
    public String getLine(int index) {
        return lines.get(index);
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
    
    public void showBackground(boolean s) {
        show_background = s;
    }
    
    public void clearText() {
        lines.clear();
    }
    
    public void draw(Graphics g) {
        if (hidden) return;
        if (alpha <= 0) return;
        
        if (title.length() == 0) {
            setHeight((lines.size()*20));
            g.setColor(background);
            g.fillRect((int)getX(), (int)getY(), getWidth(), getHeight());
            g.setColor(Color.black);
            g.setLineWidth(2);
            g.drawRect((int)getX(), (int)getY(), getWidth(), getHeight()+1);
            g.setFont(SlickInitializer.NORMAL_FONT);
            int str_h = 0, count = 3;
            for (String r: lines) {
                if ((count % 2 == 1) && count-3 < lines.size()-1) {
                    g.setColor(new Color(0, 0, 0, 100));
                    g.fillRect((int)getX(), (int)getY()+str_h+20+(int)render_offset_y, getWidth(), 20);
                }
                g.setColor(text_color.darker().darker());
                g.drawString(r, (int)getX()+6+(int)render_offset_x, (int)getY()+str_h+2+(int)render_offset_y);
                g.setColor(text_color);
                g.drawString(r, (int)getX()+5+(int)render_offset_x, (int)getY()+str_h +1+(int)render_offset_y);
                str_h+=20;
                count++;
            }
        } else {
            setHeight((lines.size()*20)+20);
            g.setColor(background);
            g.fillRect((int)getX(), (int)getY(), getWidth(), getHeight());
            g.setColor(Color.black);
            g.setLineWidth(2);
            g.drawRect((int)getX(), (int)getY(), getWidth(), getHeight()+1);
            g.setFont(SlickInitializer.NORMAL_FONT);
            g.setColor(title_color.darker().darker());
            g.drawString(title, (int)getX()+6+(int)render_offset_x, (int)getY()+3+(int)render_offset_y);
            g.setColor(title_color);
            g.drawString(title, (int)getX()+5+(int)render_offset_x, (int)getY()+2+(int)render_offset_y);
            
            int str_h = 22, count = 3;
            for (String r: lines) {
                if ((count % 2 == 1) && count-3 < lines.size()-1) {
                    g.setColor(new Color(0, 0, 0, 100));
                    g.fillRect((int)getX(), (int)getY()+str_h+20+(int)render_offset_y, getWidth(), 20);
                }

                g.setColor(text_color.darker().darker());
                g.drawString(r, (int)getX()+6+(int)render_offset_x, (int)getY()+str_h+1+(int)render_offset_y);
                g.setColor(text_color);
                g.drawString(r, (int)getX()+5+(int)render_offset_x, (int)getY()+str_h+(int)render_offset_y);
                str_h+=20;
                count++;
            }
        }

    }
    
}
