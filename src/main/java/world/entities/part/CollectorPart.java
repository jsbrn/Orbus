package world.entities.part;

import gui.GameScreen;
import misc.MiscMath;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import world.entities.Entity;
import world.entities.homebase.HomeBase;
import world.entities.player.Player;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CollectorPart extends Entity {
    
    Image spinner;
    float rotation = 0;
    
    @Override //make parts invincible
    public void addEnergy(double amount) {}
    
    @Override
    public void drainEnergy(Entity e, double multiplier) {}
    
    public CollectorPart() {
        this.setSize(64);
        this.setTexture("images/collectorpart.png");
    }
    
    public void update() {
        LinkedList<Entity> all_entities = new LinkedList<Entity>();
        all_entities.addAll(GameScreen.GAME_OBJECTS);
        for (int i = 0; i != all_entities.size(); i++) {
            if (i >= all_entities.size()) break;
            Entity e = all_entities.get(i);
            if (this.equals(e)) continue;
            if (MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                            e.getWorldCoords()[0], e.getWorldCoords()[1]) < e.getSize()) {
                if (e instanceof Player || e instanceof HomeBase) {
                    e.addPart();
                    GameScreen.deleteEntity(this, false);
                }
            }

        }
    }
    
    public void draw(Graphics g) {
        super.draw(g);
        rotation += MiscMath.getConstant(90, 1);
        if (this.spinner == null) {
            try {
                this.spinner = new Image("images/spinner.png", false, Image.FILTER_LINEAR);
            } catch (SlickException ex) {
                Logger.getLogger(CollectorPart.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            spinner.setRotation(rotation);
            g.drawImage(spinner, getRenderCoords()[0]-5, getRenderCoords()[1]-5);
        }
        g.drawImage(getTexture(), getRenderCoords()[0], getRenderCoords()[1]);
    }
}
