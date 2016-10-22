package com.bitbucket.computerology.world.entities.homebase;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.main.SlickInitializer;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.network.server.MPServer;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.animations.Animation;
import com.bitbucket.computerology.world.entities.player.Player;
import com.bitbucket.computerology.world.entities.powerups.PowerUp;
import com.bitbucket.computerology.world.entities.projectiles.EnergyBombProjectile;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class HomeBase extends Entity {
    
    double drain_count = 0.333, part_drain_timer = 0.5, add_score_timer = 1,
            call_for_help_timer = 0, anim_timer = 0;
    
    public HomeBase() {
        this.setSize(64*3);
        this.setEnergy(150);
        this.setRange(400);
        this.setTexture("resources/images/homebase.png");
    }
    
    public void update() {
        
        if (getEnergy() < 150) {
            addEnergy(MiscMath.getConstant(1, 3));
        }

        for (int i = 0; i != GameScreen.GAME_OBJECTS.size(); i++) {
            Entity e = GameScreen.GAME_OBJECTS.get(i);
            if (e != null) {
                double dist = MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                        e.getWorldCoords()[0], e.getWorldCoords()[1]);
                if (MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                        e.getWorldCoords()[0], e.getWorldCoords()[1]) < getSize()+getRange()) {
                    if (e.equals(this) || e instanceof EnergyBombProjectile || e instanceof PowerUp) continue;
                    if (getParts() >= GameScreen.getActiveTeams().size()) e.drainOrbs(this, 3); //drain the orbs if you have all the collector parts
                    if (e.getTeam().equals(getTeam())) {
                        if (e.getEnergy() < 125) {
                            e.addEnergy(MiscMath.getConstant(20*(getEnergy()/150), 1));
                        }
                        if (e instanceof Player)
                            e.drainParts(this, 1);
                    } else {
                        if (dist < getSize()+getPullRadius())
                            e.drainEnergy(this, 6);
                        if (e instanceof Player && getEnergy() < 20)
                            this.drainParts(e, 1);
                    }
                }
            }
        }
        
        add_score_timer -= MiscMath.getConstant(1, 1);
        if (add_score_timer <= 0) {
            add_score_timer = 0.5;
            getTeam().addScore(0.05*getTeam().getOrbCount()*(getEnergy()/100));
        }
        
        if (getEnergy() < 75) {
            callForHelp();
        } else if (getEnergy() > 151) {
            addEnergy(-MiscMath.getConstant(8, 1));
        }
        
    }
    
    public void drainParts(Entity e, double rate_multiplier) {
        part_drain_timer -= MiscMath.getConstant(rate_multiplier, 2);
        if (part_drain_timer > 0) return;
        part_drain_timer = 1;
        if ((int)getTeam().getPartCount() > 0) {
            removePart();
            //spawn a new part
            Entity o = Entity.create("part", -1);
            //set x to outside the circle facing the entity
            double point[] = getRotatedPoint(0, getSize() + 30, (int)MiscMath.angleBetween
        (getWorldCoords()[0], getWorldCoords()[1], e.getWorldCoords()[0], e.getWorldCoords()[1]));
            o.setX(point[0]);
            o.setY(point[1]);
            o.setVelocityFor(e.getWorldCoords()[0], e.getWorldCoords()[1], -1, -1, 200, false);
            GameScreen.addEntity(o, false, false);
        }
    }
    
    @Override
    public void addOrb() {
        getTeam().addOrb();
    }
    
    @Override
    public void removeOrb() {
        getTeam().removeOrb();
    }
    
    @Override
    public void addPart() {
        getTeam().addPart();
    }
    
    @Override
    public void removePart() {
        getTeam().removePart();
    }
    
    @Override
    public void drainOrbs(Entity e, double multiplier) {
        drain_count -= MiscMath.getConstant(multiplier, 1);
        if (drain_count > 0) return;
        drain_count = 0.333;
        if (getTeam().getOrbCount() > 0) {
            getTeam().removeOrb();
            //spawn a new orb
            Entity o = Entity.create("orb", -1);
            //set x to outside the circle facing the entity
            double point[] = getRotatedPoint(0, getSize() + 25, (int)MiscMath.angleBetween
        (getWorldCoords()[0], getWorldCoords()[1], e.getWorldCoords()[0], e.getWorldCoords()[1]));
            o.setX(point[0]);
            o.setY(point[1]);
            GameScreen.addEntity(o, false, false);
        }
    }
    
    void callForHelp() {
        call_for_help_timer -= MiscMath.getConstant(1, 1);
        if (call_for_help_timer < 0) {
            MPServer.sendTeamMessage("Your Home Base is under attack!", true, getTeam(), null);
            call_for_help_timer = 60;
        }
    }
    
    public void draw(Graphics g) {
        super.draw(g);
        g.setColor(getTeam().getTeamColor().addToCopy(new Color(0, 0, 0, -150)));
        g.fillOval(getRenderCoords()[0], getRenderCoords()[1], getSize()*2, getSize()*2);
        g.drawImage(getTexture(), getRenderCoords()[0], getRenderCoords()[1]);
        g.setColor(Color.white);
        g.setFont(SlickInitializer.NORMAL_FONT);
        String readout = "Orbs: "+(int)getOrbs()+", Parts: "+getTeam().getPartCount()+"/"+GameScreen.getActiveTeams().size();
        g.drawString("Energy: "+(int)getEnergy(), 
                (int)getOnscreenCoords()[0] 
                        - SlickInitializer.NORMAL_FONT.getWidth("Energy: "+(int)getEnergy())/2, 
                (int)getOnscreenCoords()[1]-20);
        g.drawString(readout, 
                (int)getOnscreenCoords()[0] 
                        - SlickInitializer.NORMAL_FONT.getWidth(readout)/2, 
                (int)getOnscreenCoords()[1]+5);
        if (!GameScreen.YOU.getTeam().equals(getTeam()))
            this.drawRangeIndicator(g, getPullRadius(), 1-(float)(distanceTo(GameScreen.YOU.getEntity())/getPullRadius()));
        if (getParts() >= GameScreen.getActiveTeams().size()) {
            anim_timer -= MiscMath.getConstant(1, 1);
            if (anim_timer < 0) {
                anim_timer = 1;
                Animation.create(this, getRange(), Animation.RADIATE_INWARD, getTeam(), false);
            }
        }
    }
}
