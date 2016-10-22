package com.bitbucket.computerology.world.entities.orbs;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.deployables.ForceField;
import com.bitbucket.computerology.world.entities.projectiles.EnergyBombProjectile;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;

public class EnergyOrb extends Orb {
    
    public EnergyOrb() {
        this.setSize(11);
        this.setEnergy(10);
        this.setTexture("resources/images/energyorb.png");
    }
    
    public void update() {
        if (despawn) {
            lifespan -= MiscMath.getConstant(1, 1);
            if (lifespan < 0) GameScreen.deleteEntity(this, false);
        }
        Entity entity = null;
        LinkedList<Entity> all_entities = new LinkedList<Entity>();
        all_entities.addAll(GameScreen.GAME_OBJECTS);
        for (int i = 0; i != all_entities.size(); i++) {
            if (i >= all_entities.size()) break;
            Entity e = all_entities.get(i);
            if (this.equals(e)) continue;
            
            if (MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                            e.getWorldCoords()[0], e.getWorldCoords()[1]) < getSize()+e.getSize()) {
                if (e instanceof EnergyBombProjectile || e instanceof ForceField) continue;
                e.addEnergy(10);
                GameScreen.deleteEntity(this, false);
            }

        }
    }
}
