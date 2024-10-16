package gui.elements;

import main.SlickInitializer;
import misc.MiscString;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ToolTip extends GUIElement {
    
    String[] rows;
    
    public ToolTip(int width) {
        this.setWidth(200);
        this.rows = new String[]{};
    }
    
    public void setText(String text) {
        if (text == null) { rows = new String[]{}; return; }
        if (text.length() == 0) { rows = new String[]{}; return; }
        rows = MiscString.wrap(text, 200, SlickInitializer.SMALL_FONT);
    }
    
    public void draw(Graphics g) {
        
        if (rows.length == 0) return;
        
        int row_y = 0;
        int width = 0;
        for (String r: rows) {
            if (SlickInitializer.SMALL_FONT.getWidth(r) + 10 > width) {
                width = SlickInitializer.SMALL_FONT.getWidth(r) + 10;
            }
        }
        
        setWidth(width);
        setHeight((rows.length*20) + 5);
        
        g.setColor(Color.gray.darker());
        g.fillRect((int)getX()+3, (int)getY()+3, width, (rows.length*20) + 5);
        g.setColor(new Color(235, 230, 190));
        g.fillRect((int)getX(), (int)getY(), width, (rows.length*20) + 5);
        g.setLineWidth(1);
        g.setColor(Color.black);
        g.drawRect((int)getX(), (int)getY(), width, (rows.length*20) + 5);
        
        g.setFont(SlickInitializer.SMALL_FONT);
        for (String r: rows) {
            g.drawString(r, (int)getX() + 5, (int)getY()+ row_y + 5);
            row_y += 20;
        }
    }
    
}
