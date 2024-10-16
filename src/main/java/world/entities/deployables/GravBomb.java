package world.entities.deployables;

import gui.GameScreen;
import misc.MiscMath;
import org.newdawn.slick.Graphics;
import world.entities.Entity;
import world.entities.homebase.HomeBase;
import world.entities.orbs.EnergyOrb;
import world.entities.player.Player;

import java.util.LinkedList;

public class GravBomb extends Entity {
    
    double anim_timer = 0, pull_timer = 0, lifespan = 300;
    
    public GravBomb() {
        this.setSize(16);
        this.setEnergy(300);
        this.setRange(0);
        this.setTexture("images/gravbomb.png");
    }
    
    public void onDelete() {dropGoodies();}
    
    public void update() {
        LinkedList<Entity> all_entities = new LinkedList<Entity>();
        all_entities.addAll(GameScreen.GAME_OBJECTS);
        for (int i = 0; i != all_entities.size(); i++) {
            if (i >= all_entities.size()) break;
            Entity e = all_entities.get(i);
            if (e != null) {
                if (e instanceof Player || e instanceof HomeBase || e instanceof Sidekick) {
                    if (MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                            e.getWorldCoords()[0], e.getWorldCoords()[1]) < getSize() + getPullRadius()) {
                        if (e.getTeam().equals(this.getTeam()) == false) {
                            e.drainOrbs(this, 1);
                        } else {
                            this.drainOrbs(e, 1);
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
                if (e instanceof EnergyOrb == false) {
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
        lifespan-=MiscMath.getConstant(4, 1);
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
