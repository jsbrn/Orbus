package com.bitbucket.computerology.world.entities.deployables;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.SoundManager;
import com.bitbucket.computerology.network.Packet.UpdateEntity;
import com.bitbucket.computerology.network.Packet.UpdateMovementVector;
import com.bitbucket.computerology.network.server.MPServer;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.animations.Animation;
import com.bitbucket.computerology.world.entities.homebase.HomeBase;
import com.bitbucket.computerology.world.entities.orbs.Orb;
import com.bitbucket.computerology.world.entities.player.Player;
import org.newdawn.slick.Graphics;

public class Teleporter extends Entity {
    
    double shoot_timer = 1;
    static int teleports = 0;
    boolean teleported;
    
    public Teleporter() {
        this.setSize(2);
        this.setEnergy(0);
        this.teleported = false;
    }
    
    public void onSpawn() {
        SoundManager.playSound(SoundManager.TELEPORT, getWorldCoords()[0], getWorldCoords()[1]);
    }
    
    @Override
    public void update() {
        if (teleported) return;
        
        Entity closest = null;
        double short_dist = 200000000;
        for (int i = 0; i != GameScreen.PLAYERS.size(); i++) {
            if (i >= GameScreen.PLAYERS.size()) break;
            Entity e = GameScreen.PLAYERS.get(i).getEntity();
            if (e != null) {
                double dist = MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                                e.getWorldCoords()[0], e.getWorldCoords()[1]);
                if (dist < 20 && dist < short_dist) {
                    closest = e;
                    short_dist = dist;
                }
            }
        }
        
        if (closest != null) {
            HomeBase hb = closest.getTeam().getHomeBase();
            if (hb != null) {
                Camera.SPEED = 0.1;
                closest.setX(hb.getWorldCoords()[0]);
                closest.setY(hb.getWorldCoords()[1]);
                closest.setEnergy(100);
                UpdateEntity ue = new UpdateEntity();
                ue.energy = closest.getEnergy();
                ue.id = closest.getID();
                ue.orb = closest instanceof Orb;
                ue.x = closest.getWorldCoords()[0];
                ue.y = closest.getWorldCoords()[1];
                ue.orbs = closest.getOrbs();
                ue.parts = closest.getParts();
                MPServer.sendToAllClients(ue, false);
                MPServer.sendTeamMessage(((Player)closest).getOwner().getName()+" has warped to Home Base!", 
                        false, closest.getTeam(), ((Player)closest).getOwner());
                Animation.create(getWorldCoords()[0], getWorldCoords()[1], getSize()+20, Animation.RADIATE_INWARD, 
                        getTeam(), true);
            }
            GameScreen.deleteEntity(this, false);
            teleported = true;
        }
    }
    
    public void draw(Graphics g) {
        
    }
}
