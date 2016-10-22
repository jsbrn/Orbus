package com.bitbucket.computerology.world.entities.deployables;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.animations.Animation;
import com.bitbucket.computerology.world.entities.orbs.EnergyOrb;
import com.bitbucket.computerology.world.entities.part.CollectorPart;
import com.bitbucket.computerology.world.entities.powerups.PowerUp;
import com.bitbucket.computerology.world.entities.projectiles.EnergyBombProjectile;
import java.util.LinkedList;
import org.newdawn.slick.Graphics;

public class EnergyBomb extends Entity {
    
    double anim_timer = 0, pull_timer = 0, lifespan = 300;
    
    public void onDelete() {dropGoodies();}
    
    public EnergyBomb() {
        this.setSize(16);
        this.setEnergy(250);
        this.setRange(0);
        this.setTexture("resources/images/energybomb.png");
    }
    
    public void update() {
        
        LinkedList<Entity> all_entities = new LinkedList<Entity>();
        all_entities.addAll(GameScreen.GAME_OBJECTS);
        for (int i = 0; i != all_entities.size(); i++) {
            if (i >= all_entities.size()) break;
            Entity e = all_entities.get(i);
            if (e != null) {
                if (e instanceof PowerUp || e instanceof EnergyBombProjectile
                        || e instanceof ForceField || e instanceof EnergyBomb || e instanceof CollectorPart) continue;
                if (MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                        e.getWorldCoords()[0], e.getWorldCoords()[1]) < getSize() + getPullRadius()
                        && e.equals(this) == false) {
                    if (e.getTeam().equals(this.getTeam()) == false) {
                        e.drainEnergy(this, 3);
                    } else {
                        if (getEnergy() > 225) {
                            this.drainEnergy(e, 3);
                        }
                    }
                }
                
            }
        }
        pull_timer -= MiscMath.getConstant(2, 1);
        LinkedList<Entity> all_orbs = new LinkedList<Entity>();
        all_orbs.addAll(GameScreen.ORBS);
        for (int i = 0; i != all_orbs.size(); i++) {
            if (i >= all_orbs.size()) break;
            Entity e = all_orbs.get(i);
            if (e != null) {
                if (e instanceof EnergyOrb) {
                    if (MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                            e.getWorldCoords()[0], e.getWorldCoords()[1]) < getSize() + getPullRadius()) {
                        if (pull_timer < 0 && Math.abs(e.getMovementVector()[0]) < 5
                                && Math.abs(e.getMovementVector()[1]) < 5) {
                            pull_timer = 1;
                            e.setVelocityFor(getWorldCoords()[0], getWorldCoords()[1], 0, 0, 250, true);
                        }
                    }
                }
            }
        }
        //slowly deplete energy
        lifespan-=MiscMath.getConstant(3, 1);
        addEnergy(-MiscMath.getConstant(1, 3));
        if (lifespan < 0) {
            GameScreen.deleteEntity(this, false);
        }
    }
    
    public void draw(Graphics g) {
        super.draw(g);
        drawOrbs(g);
        g.setColor(getTeam().getTeamColor());
        g.fillOval((float)getRenderCoords()[0]+2, (float)getRenderCoords()[1]+2, -4+getSize()*2, -4+getSize()*2);
        g.drawImage(getTexture(), getRenderCoords()[0], getRenderCoords()[1]);
        this.drawRangeIndicator(g, getPullRadius(), 1-(float)(distanceTo(GameScreen.YOU.getEntity())/getPullRadius()));
    }
}
