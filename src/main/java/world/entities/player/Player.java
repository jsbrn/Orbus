package world.entities.player;

import gui.GameScreen;
import main.SlickInitializer;
import misc.MiscMath;
import network.server.MPServer;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import world.PlayerCard;
import world.entities.Entity;
import world.entities.powerups.PowerUp;

import java.util.LinkedList;

public class Player extends Entity {
    
    PlayerCard owner;
    double call_for_help_timer = 0, anim_timer = 0, bot_update_timer = 0;
    
    public Player() {
        this.setSize(64);
        this.setTexture("images/player.png");
    }
    
    public void update() {
        drainNearbyEnemies();
        
        if (owner == null) { GameScreen.deleteEntity(this, false); return; }
        bot_update_timer -= MiscMath.getConstant(5, 1);
        if (bot_update_timer <= 0) {
            bot_update_timer = 1;
            owner.update(); //if owner is a bot, this will run the bot behaviour
        }
        
        LinkedList<Entity> colliding = GameScreen.getEntities(
                getWorldCoords()[0], getWorldCoords()[1], getSize(), this);
        for (int i = 0; i != colliding.size(); i++) {
            if (i >= colliding.size()) break;
            Entity e = colliding.get(i);
            if (e instanceof PowerUp) {
                if (owner.powerupCount() < PlayerCard.POWERUP_LIMIT) {
                    PowerUp p = ((PowerUp)e);
                    p.grab(this);
                }
            }
        }
        double energy_to_add = 0;
        if (getEnergy() < 100) {
            energy_to_add += MiscMath.getConstant(2, 1)*(1+(getEnergy()/50));
        }
        
        if (getEnergy() < 30) {
            call_for_help_timer -= MiscMath.getConstant(1, 1);
            if (call_for_help_timer < 0) {
                MPServer.sendTeamMessage(getOwner().getName()+"'s energy levels are critical!", true, getTeam(), getOwner());
                call_for_help_timer = 30;
            }
        } else if (getEnergy() > 151) {
            energy_to_add += -MiscMath.getConstant(10, 1);
        }
        
        if (owner.isKeyPressed(Input.KEY_LSHIFT) && getEnergy() > 5) 
            energy_to_add += -MiscMath.getConstant(10, 1);
        addEnergy(energy_to_add);
    }
    
    public void setOwner(PlayerCard p) {
        owner = p;
    }
    
    public PlayerCard getOwner() {
        return owner;
    }
    
    @Override
    public void onDelete() {dropGoodies();}
    
    public void draw(Graphics g) {
        if (owner == null) return;
        super.draw(g);
        drawOrbs(g);
        drawParts(g);
        g.setColor(getTeam().getTeamColor());
        g.fillOval((float)getRenderCoords()[0]+2, (float)getRenderCoords()[1]+2, (getSize()-2)*2, (getSize()-2)*2);
        g.setColor(Color.white);
        g.drawImage(getTexture(), getRenderCoords()[0], getRenderCoords()[1]);
        
        g.setFont(SlickInitializer.NORMAL_FONT);
        g.setColor(Color.white);
        if (GameScreen.YOU.getEntity().equals(this) == false) {
            g.drawString(owner.getName(), 
                    (int)getOnscreenCoords()[0] 
                            - SlickInitializer.NORMAL_FONT.getWidth(owner.getName())/2, 
                    (int)getOnscreenCoords()[1]-10);
        }
        if (GameScreen.YOU.equals(owner)) {
            double energy_to_add = 0;
            if (getEnergy() < 100) {
                energy_to_add += MiscMath.getConstant(2, 1)*(1+(getEnergy()/50));
            }

            if (getEnergy() < 30) {
                call_for_help_timer -= MiscMath.getConstant(1, 1);
                if (call_for_help_timer < 0) {
                    MPServer.sendTeamMessage(getOwner().getName()+"'s energy levels are critical!", true, getTeam(), getOwner());
                    call_for_help_timer = 30;
                }
            } else if (getEnergy() > 151) {
                energy_to_add += -MiscMath.getConstant(10, 1);
            }

            if (owner.isKeyPressed(Input.KEY_LSHIFT) && getEnergy() > 5) 
                energy_to_add += -MiscMath.getConstant(10, 1);
            addEnergy(energy_to_add);
        }
    }
    
}
