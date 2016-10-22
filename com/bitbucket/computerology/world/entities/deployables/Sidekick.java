package com.bitbucket.computerology.world.entities.deployables;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.network.server.MPServer;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.homebase.HomeBase;
import com.bitbucket.computerology.world.entities.orbs.Orb;
import com.bitbucket.computerology.world.entities.powerups.PowerUp;
import java.util.LinkedList;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class Sidekick extends Entity {
    
    boolean drain = false;
    
    public Sidekick() {
        this.setSize(16);
        this.setEnergy(75);
        this.setTexture("resources/images/sidekick.png");
    }
    
    public void onDelete() {dropGoodies();}
    
    public void update() {
        
        if (!drain) {
            Entity closest = null;
            double short_dist = 200000000;
            LinkedList<Orb> all_orbs = new LinkedList<Orb>();
            all_orbs.addAll(GameScreen.ORBS);
            for (int i = 0; i != all_orbs.size(); i++) {
                if (i >= all_orbs.size()) break;
                Entity e = all_orbs.get(i);
                if (e != null) {
                    double dist = MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                                    e.getWorldCoords()[0], e.getWorldCoords()[1]);
                    HomeBase hb = getTeam().getHomeBase();
                    double hb_dist = MiscMath.distanceBetween(hb.getWorldCoords()[0], hb.getWorldCoords()[1], 
                                    e.getWorldCoords()[0], e.getWorldCoords()[1]);
                    if (e instanceof PowerUp == false) {
                        if (e.getTeam().equals(this.getTeam()) == false) {
                            if (dist < short_dist && hb_dist > 500) {
                                closest = e;
                                short_dist = dist;
                                if (dist < 500) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (closest != null) {
                this.setVelocityFor(closest.getWorldCoords()[0], closest.getWorldCoords()[1], 0, 0, 150, true);
            }
        } else {
            Entity hb = getTeam().getHomeBase();
            if (hb != null) {
                if (hb.distanceTo(this) <= 100) {
                    this.setVelocityFor(0, 0, 0, 0, 0, true);
                } else {
                    this.setVelocityFor(hb.getWorldCoords()[0], hb.getWorldCoords()[1], 0, 0, 170, true);
                }
            }
        }
        
        if (getOrbs() <= 0 && drain) {
            drain = false;
        }
        if (!drain && getOrbs() >= 15) {
            drain = true;
        }
        
        if (getEnergy() > 60) {
            addEnergy(-MiscMath.getConstant(2, 1));
        }
        
    }
    
    @Override
    public void addEnergy(double amount) {
        super.addEnergy(amount);
        if (getEnergy() <= 0 && GameScreen.YOU.isHost()) {
            MPServer.sendTeamMessage("A "+getTeam().getName()+" Sidekick has died!", true, getTeam(), null);
        }
    }
    
    public void draw(Graphics g) {
        super.draw(g);
        drawOrbs(g);
        g.setColor(getTeam().getTeamColor());
        g.fillOval((float)getRenderCoords()[0], (float)getRenderCoords()[1], getSize()*2, getSize()*2);
        g.drawImage(getTexture(), getRenderCoords()[0], getRenderCoords()[1]);
    }
}
