package world.entities.powerups;

import gui.GameScreen;
import misc.SoundManager;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import world.entities.Entity;
import world.entities.player.Player;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PowerUp extends Entity {
    
    public static int MODIFIER = 0, GRAV_BOMB = 1, ENERGY_BOMB = 2, SIDEKICK = 3, TURRET = 4, TELEPORTER = 5, FORCE_FIELD = 6;
    public static int SPEED = 1, BASE_ENERGY = 2;
    int type = -1;
    public static String[] NAMES = {"Modifier", "Gravity Bomb", "Energy Bomb", "Sidekick", "Turret", "Teleporter"
    , "Force Field"};
    public static Image[] ICONS;
    
    public PowerUp(int type) {
        this.type = type;
        this.setSize(16);
        this.setTexture("images/powerup.png");
    }
    
    public void grab(Player grabber) {
        if (type == MODIFIER) {
            //change a player stat
        } else {
            grabber.getOwner().addPowerup(type);
        }
        GameScreen.deleteEntity(this, false);
        GameScreen.spawnPowerup(false);
    }
    
    public int getType() {
        return type;
    }
    
    @Override
    public void onDelete() {
        SoundManager.playSound(SoundManager.POWERUP, getWorldCoords()[0], getWorldCoords()[1]);
    }
    
    @Override
    public void drainEnergy(Entity e, double rate) {}
    
    public void draw(Graphics g) {
        super.draw(g);
        g.drawImage(getTexture(), getRenderCoords()[0], getRenderCoords()[1]);
        if (ICONS == null) {
            ICONS = new Image[7];
            ICONS[0] = null;
            try {
                ICONS[1] = new Image("images/gravbombpowerup.png");
                ICONS[2] = new Image("images/energybombpowerup.png");
                ICONS[3] = new Image("images/sidekickpowerup.png");
                ICONS[4] = new Image("images/turretpowerup.png");
                ICONS[5] = new Image("images/teleportpowerup.png");
                ICONS[6] = new Image("images/forcefieldpowerup.png");
            } catch (SlickException ex) {
                Logger.getLogger(PowerUp.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
