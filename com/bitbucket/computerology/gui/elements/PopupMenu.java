package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.main.SlickInitializer;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.MiscString;
import com.bitbucket.computerology.misc.Window;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class PopupMenu extends GUIElement {

    Image background;
    String text, title;
    ArrayList<Button> buttons;
    String[] rows;
    float alpha = 0, offset_y = -15;
    boolean fade_in = false; //fade out if false, fade in if true
    
    public PopupMenu() {
        this.text = "";
        this.title = "Alert";
        this.buttons = new ArrayList<Button>();
        rows = new String[]{""};
        try {
            this.background = new Image("images/gui/popup.png", false, Image.FILTER_NEAREST).getScaledCopy(2);
            this.setWidth(background.getWidth());
            this.setHeight(background.getHeight());
        } catch (SlickException ex) {
            Logger.getLogger(PopupMenu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addButton(Button b) {
        buttons.add(b);
    }
    
    public void show() {
        fade_in = true;
    }
    
    public void hide() {
        fade_in = false;
    }
    
    public void update() {
        if (fade_in) {
            alpha+=MiscMath.getConstant(8, 1);
        } else {
            alpha-=MiscMath.getConstant(8, 1);
        }
        
        if (alpha > 1) {
            alpha = 1;
            offset_y = 0;
        } else if (alpha < 0) {
            alpha = 0;
            offset_y = -15;
        }
        
        if (alpha != 0 && alpha != 1) {
            offset_y += MiscMath.getConstant(120, 1);
        }
    
        for (Button b: buttons) {
            b.setY(getY()+getHeight() - 55+offset_y);
            b.setX(getX() + 10);
        }
    }
    
    public int getClickedButton(int mouse_button) {
        if (isVisible()) {
            for (int i = 0; i != buttons.size(); i++) {
                if (buttons.get(i).isClicked(mouse_button)) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setText(String text) {
        this.text = text;
        rows = MiscString.wrap(text, background.getWidth()-40, SlickInitializer.NORMAL_FONT);
    }
    
    @Override
    public boolean isVisible() {
        return !(alpha == 0);
    }
    
    public void draw(Graphics g) {
        
        g.setFont(SlickInitializer.NORMAL_FONT);
        g.setColor(new Color(0, 0, 0, alpha/2));
        g.fillRect(0, 0, Window.getWidth(), Window.getHeight());
        
        background.setAlpha(alpha);
        g.drawImage(background, (int)getX(), (int)getY()+offset_y);
        g.setColor(Color.gray.darker());
        g.drawString(title, (int)getX() + 1 + (getWidth() / 2) - (SlickInitializer.NORMAL_FONT.getWidth(title)/2), 
                (int)getY()+10+offset_y);
        g.setColor(new Color(255, 255, 255, alpha));
        g.drawString(title, (int)getX() + (getWidth() / 2) - (SlickInitializer.NORMAL_FONT.getWidth(title)/2), 
                (int)getY()+9+offset_y);
        
        g.setColor(new Color(0, 0, 0, alpha));
        int row_y = 0;
        if (rows != null) {
            for (String r: rows) {
                if (r != null) {
                    g.drawString(r, (int)getX()+20, (int)getY()+50+row_y+offset_y);
                    row_y += 20;
                }
            }
        }
        
        for (Button b: buttons) {
            b.draw(g);
        }
        
    }

}
