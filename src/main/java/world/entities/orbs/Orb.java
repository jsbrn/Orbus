package world.entities.orbs;

import gui.GameScreen;
import misc.MiscMath;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import world.entities.Entity;
import world.entities.deployables.GravBomb;
import world.entities.deployables.Sidekick;
import world.entities.homebase.HomeBase;
import world.entities.player.Player;

import java.util.LinkedList;

public class Orb extends Entity {
    
    double lifespan = 10;
    boolean despawn = false;
    int[] rgb = new int[]{255, 255, 255, 1, 1, -1};
    
    public Orb() {
        this.setSize(8);
        this.setTexture("images/orb.png");
    }
    
    public void enableDespawn() {
        despawn = true;
    }
    
    public void update() {
        if (despawn) {
            lifespan -= MiscMath.getConstant(1, 1);
            if (lifespan < 0) GameScreen.deleteEntity(this, false);
        }
        LinkedList<Entity> all_entities = new LinkedList<Entity>();
        all_entities.addAll(GameScreen.GAME_OBJECTS);
        for (int i = 0; i != all_entities.size(); i++) {
            if (i >= all_entities.size()) break;
            Entity e = all_entities.get(i);
            if (this.equals(e)) continue;
            if (MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                            e.getWorldCoords()[0], e.getWorldCoords()[1]) < getSize()+e.getSize()) {
                if (e instanceof Player || e instanceof Sidekick 
                        || e instanceof HomeBase || e instanceof GravBomb) {
                    e.addOrb();
                    GameScreen.deleteEntity(this, false);
                }
            }

        }
    }
    
    public void draw(Graphics g) {
        super.draw(g);
        rgb[0] += MiscMath.getConstant(rgb[3]*255, 4);
        rgb[1] += MiscMath.getConstant(rgb[4]*255, 6);
        rgb[2] += MiscMath.getConstant(rgb[5]*255, 2);
        if (rgb[0] < 40) { rgb[0] = 40; rgb[3] = 1; } else if (rgb[0] > 255) { rgb[0] = 255; rgb[3] = -1; } 
        rgb[1] += MiscMath.getConstant(rgb[4]*255, 5);
        if (rgb[1] < 60) { rgb[1] = 60; rgb[4] = 1; } else if (rgb[1] > 255) { rgb[1] = 255; rgb[4] = -1; } 
        rgb[2] += MiscMath.getConstant(rgb[5]*255, 5);
        if (rgb[2] < 10) { rgb[2] = 10; rgb[5] = 1; } else if (rgb[2] > 255) { rgb[2] = 255; rgb[5] = -1; } 
        g.setColor(new Color(rgb[0], rgb[1], rgb[2], 100));
        if (this instanceof EnergyOrb == false) g.fillOval((float)getRenderCoords()[0]+2, (float)getRenderCoords()[1]+2, getSize()*2, getSize()*2);
        g.drawImage(getTexture(), getRenderCoords()[0], getRenderCoords()[1]);
    }
}
