package com.bitbucket.computerology.world.entities.projectiles;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.SoundManager;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.part.CollectorPart;
import com.bitbucket.computerology.world.entities.powerups.PowerUp;

import java.util.LinkedList;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class EnergyBombProjectile extends Entity {
    
    double lifespan = 1;
    
    public EnergyBombProjectile() {
        this.setSize(8);
        this.setEnergy(300);
        this.setRange(150);
        this.setTexture("resources/images/projectile.png");
    }
    
    public void onSpawn() {
        SoundManager.playSound(SoundManager.SHOOT, getWorldCoords()[0], getWorldCoords()[1]);
    }
    
    public void update() {
        LinkedList<Entity> all_entities = new LinkedList<Entity>();
        all_entities.addAll(GameScreen.GAME_OBJECTS);
        for (int i = 0; i != all_entities.size(); i++) {
            if (i >= all_entities.size()) break;
            Entity e = all_entities.get(i);
            if (e != null) {
                if (e instanceof PowerUp || e instanceof CollectorPart) continue;
                if (e.getTeam().equals(this.getTeam()) == false) {
                    double dist = MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                            e.getWorldCoords()[0], e.getWorldCoords()[1]);
                    if (dist < getRange()
                            && e.equals(this) == false) {
                            if (dist < e.getSize() + getSize()) {
                                //colliding
                                e.addEnergy(-10);
                                GameScreen.deleteEntity(this, false);
                            }
                    }
                }
            }
        }
        
        //limited lifespan
        lifespan-=MiscMath.getConstant(1, 2);
        if (lifespan <= 0) {
            GameScreen.deleteEntity(this, false);
        }
    }
    
    public void draw(Graphics g) {
        super.draw(g);
        g.setColor(getTeam().getTeamColor());
        g.fillOval((float)getRenderCoords()[0], (float)getRenderCoords()[1], getSize()*2, getSize()*2);
        g.drawImage(getTexture(), getRenderCoords()[0], getRenderCoords()[1]);
    }
}
