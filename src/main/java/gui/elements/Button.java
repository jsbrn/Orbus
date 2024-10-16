package gui.elements;

import main.SlickInitializer;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A clickable {@link GUIElement}. Performs an action once every time it is clicked.
 * @see Button#isClicked(int mouse_button)
 */
public class Button extends GUIElement {

    String title;
    //Color color;
    boolean active;
    Image border;

    /**
     * Creates a button with an icon derived from a String.
     * @param title The button title.
     */
    public Button(String title) {
        this.title = title;
        setHeight(32);
        try {
            this.border = new Image("images/buttonborder.png", false, Image.FILTER_LINEAR);
        } catch (SlickException ex) {
            Logger.getLogger(Button.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets the title of the button (not the subtitle).
     * @param s The new title.
     */
    public void setText(String s) {
        title = s;
    }

    /**
     * Whether or not the button is activated.
     * @return A boolean indicating if the button is activated.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the button as activated (visually renders with a different color).
     */
    public void activate() {
        active = true;
    }

    /**
     * Get the button title. The title is rendered on top of the subtitle when button text is set to render.
     * @return The button title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the button as deactivated.
     */
    public void deactivate() {
        active = false;
    }

    public void draw(Graphics g) {

        setWidth((SlickInitializer.NORMAL_FONT.getWidth(title)+ 10));
        
        if (hidden == false) {
            
            int x = 8;
            for (int i = 0; i != getHeight(); i++) {
                int dist = (10*Math.abs(getHeight()/2-i));
                g.setColor(new Color(dist, dist, dist, 200));
                if (isHovered() && enabled) {
                    g.setColor(new Color(75+dist/2, 50+dist/2, 200+dist/2, 180));
                }
                g.fillRect((int)X+x, (int)Y+i, W-(x*2), 1);
                if (i >= 7 && i < 23) {
                    x = 1;
                } else if (i <= 7) {
                    x--;
                } else if (i >= 23) {
                    x++;
                }
            }
            g.setColor(new Color(0, 0, 0));
            g.drawImage(border.getSubImage(0, 0, 16, 32), (int)getX(), (int)getY());
            g.drawImage(border.getSubImage(16, 0, 16, 32), (int)getX()+getWidth()-16, (int)getY());
            g.drawImage(border.getSubImage(15, 0, 2, 32).getScaledCopy(getWidth()-20, getHeight()), (int)getX()+10, (int)getY());
            int text_x = 0, text_y = 0;
            if (title != null) {
                text_x = (int)getX() + (getWidth() / 2) -  (SlickInitializer.NORMAL_FONT.getWidth(title) / 2);
                text_y = (int)getY() + 7;
                g.setFont(SlickInitializer.NORMAL_FONT);
                g.setColor(Color.white.darker().darker().darker());
                g.drawString(title, text_x+1, text_y+1);
                g.setColor(Color.white);
                g.drawString(title, text_x, text_y);
                g.setColor(Color.white);
            }

        }

    }

}
