package com.bitbucket.computerology.world.entities.deployables;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.SoundManager;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.homebase.HomeBase;
import com.bitbucket.computerology.world.entities.player.Player;
import com.bitbucket.computerology.world.entities.powerups.PowerUp;
import com.bitbucket.computerology.world.entities.projectiles.EnergyBombProjectile;
import java.util.LinkedList;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class ForceField extends Entity {
    
    double render_multiplier = 0.1, deplete_timer = 1;
    boolean setup = false;
    
    public ForceField() {
        this.setSize(64*6);
        this.setEnergy(200);
        this.setRange(500);
        
    }
    
    @Override
    public void onSpawn() {
        SoundManager.playSound(SoundManager.FORCEFIELD_SPAWN, getWorldCoords()[0], getWorldCoords()[1]);
    }
    
    public void update() {
        
        LinkedList<Entity> entities = GameScreen.getEntities(getWorldCoords()[0], getWorldCoords()[1], getSize(), this);
        boolean touching_enemy = false;
        for (int i = 0; i != entities.size(); i++) {
            Entity e = entities.get(i);
            if (e instanceof EnergyBombProjectile) {
                if (e.getTeam().getTeamCode() != this.getTeam().getTeamCode()) {
                    GameScreen.deleteEntity(e, false);
                    this.addEnergy(-2);
                }
            } else if (((e instanceof Player && e.getTeam().getTeamCode() == getTeam().getTeamCode()) || 
                    (e instanceof HomeBase && e.getTeam().getTeamCode() == getTeam().getTeamCode()) || e instanceof PowerUp) == false) {
                if (setup == false) {
                    this.addEnergy(-5);
                    touching_enemy = true;
                }
            }
        }
        
        if (getEnergy() < 20) { GameScreen.deleteEntity(this, false); System.out.println("forcefield is kil");}
        
        if (!touching_enemy && render_multiplier >= 1) {
            setup = true;
        }
        
        deplete_timer -= MiscMath.getConstant(1, 5);
        if (deplete_timer <= 0) {
            addEnergy(-5);
            deplete_timer = 1;
        }
        
    }
    
    public void draw(Graphics g) {
        setSize((int)((getEnergy()/200)*64*6));
        float size = (float)(getSize()*render_multiplier);
        float[] render = new float[]{(float)getOnscreenCoords()[0]-size, (float)getOnscreenCoords()[1]-size};
        g.setColor(Color.black);
        g.drawOval(render[0]-1, (float)render[1]-1, 2+size*2, 2+size*2);
        g.setColor(getTeam().getTeamColor());
        g.setLineWidth(2);
        g.drawOval((float)render[0], (float)render[1], size*2, size*2);
        g.setColor(new Color(80, 225, 250, 100));
        g.fillOval((float)render[0]+5, (float)render[1]+5, -10+size*2, -10+size*2);
        g.setLineWidth(1);
        render_multiplier += MiscMath.getConstant(1, 0.5);
        if (render_multiplier > 1) render_multiplier = 1;
        if (GameScreen.YOU.canSee(this)) {
            SoundManager.loopSound(SoundManager.FORCEFIELD_AMBIENT, getWorldCoords()[0], getWorldCoords()[1]);
        }
    }
}
