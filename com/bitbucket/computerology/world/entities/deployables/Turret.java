package com.bitbucket.computerology.world.entities.deployables;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.TeamCard;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.part.CollectorPart;
import com.bitbucket.computerology.world.entities.powerups.PowerUp;
import com.bitbucket.computerology.world.entities.projectiles.EnergyBombProjectile;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class Turret extends Entity {
    
    double shoot_timer = 1, anim_timer = 0;
    float angle = 0;
    
    public Turret() {
        this.setSize(16);
        this.setEnergy(100);
        this.setRange(750);
        this.setTexture("resources/images/turret.png");
    }
    
    public void onDelete() {dropGoodies();}
    
    public void update() {
        Entity closest = getClosest();
        shoot_timer -= MiscMath.getConstant(2, 1);
        if (closest != null && shoot_timer < 0) {
            shoot_timer = 2/((10+getEnergy())/75);
            Entity p = Entity.create("projectile", -1);
            p.setX(this.getWorldCoords()[0]);
            p.setY(this.getWorldCoords()[1]);
            p.setTeam(this.getTeam());
            p.setVelocityFor(closest.getWorldCoords()[0], closest.getWorldCoords()[1], 0, 0, 350, false);
            GameScreen.addEntity(p, false, false);
        }
    }
    
    Entity getClosest() {
        Entity closest = null;
        double short_dist = 200000000;
        LinkedList<Entity> all_entities = new LinkedList<Entity>();
        all_entities.addAll(GameScreen.GAME_OBJECTS);
        for (int i = 0; i != all_entities.size(); i++) {
            if (i >= all_entities.size()) break;
            Entity e = all_entities.get(i);
            if (e != null) {
                double dist = MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                                e.getWorldCoords()[0], e.getWorldCoords()[1]);
                if (e instanceof PowerUp == false && e instanceof CollectorPart == false) {
                    if (e.getTeam().equals(this.getTeam()) == false 
                            && e instanceof EnergyBombProjectile == false
                            && e instanceof ForceField == false) {
                        if (dist < getSize() + getRange() && dist < short_dist) {
                            closest = e;
                            short_dist = dist;
                        }
                    }
                }
            }
        }
        return closest;
    }
    
    public void draw(Graphics g) {
        super.draw(g);
        g.setColor(getTeam().getTeamColor());
        g.fillRoundRect((float)getRenderCoords()[0], (float)getRenderCoords()[1], getSize()*2, getSize()*2, 13);
        g.drawImage(getTexture(), getRenderCoords()[0], getRenderCoords()[1]);
        Entity closest = getClosest();
        float a = 0;
        if (closest != null) {
            a = (float)MiscMath.angleBetween(getWorldCoords()[0], getWorldCoords()[1], closest.getWorldCoords()[1], 
                    closest.getWorldCoords()[1]);
            if (angle >= a) angle -= MiscMath.getConstant(30, 1); else angle += MiscMath.getConstant(30, 1);
        } else {
            angle+= MiscMath.getConstant(30, 1);
        }
        double[] offset = getRotatedOffset(0, 40, (int)(angle));
        g.setLineWidth(8);
        g.setColor(Color.black);
        g.drawLine((float)getOnscreenCoords()[0], (float)getOnscreenCoords()[1], 
                (float)(getOnscreenCoords()[0]+offset[0]), (float)(getOnscreenCoords()[1]+offset[1]));
        g.setColor(Color.white);
        g.setLineWidth(1);
    }
}
