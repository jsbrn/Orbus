package com.bitbucket.computerology.misc;

import com.bitbucket.computerology.gui.GameScreen;
import java.util.LinkedList;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;

public class SoundManager {
    
    public static LinkedList<Sound> ALL_SOUNDS = new LinkedList<Sound>();
    public static int SHOOT = 0, SHOOT_FAIL = 1, POWERUP = 2, DYING = 3, 
            FAILURE = 4, VICTORY = 5, TELEPORT = 6, FORCEFIELD_SPAWN = 7, 
            FORCEFIELD_AMBIENT = 8;
    
    public static void loadSounds() {
        try {
            ALL_SOUNDS.add(new Sound("resources/sounds/shoot.ogg"));
            ALL_SOUNDS.add(new Sound("resources/sounds/shoot.ogg"));
            ALL_SOUNDS.add(new Sound("resources/sounds/powerup.ogg"));
            ALL_SOUNDS.add(new Sound("resources/sounds/dying.ogg"));
            ALL_SOUNDS.add(new Sound("resources/sounds/failure.ogg"));
            ALL_SOUNDS.add(new Sound("resources/sounds/victory.ogg"));
            ALL_SOUNDS.add(new Sound("resources/sounds/teleport.ogg"));
            ALL_SOUNDS.add(new Sound("resources/sounds/forcefield_spawn.ogg"));
            ALL_SOUNDS.add(new Sound("resources/sounds/forcefield_ambient.ogg"));
        } catch (SlickException ex) {
            ex.getLocalizedMessage();
        }
    }

    public static void loopSound(int id, double world_x, double world_y) {
        if (id < 0 || id >= ALL_SOUNDS.size()) return;
        Sound s = ALL_SOUNDS.get(id);
        if (s != null) {
            if (s.playing() == false) {
                SoundManager.playSound(id, world_x, world_y);
            }
        }
    }
    
    public static void playSound(int id, double world_x, double world_y) {
        
        float volume = 1;
        if (GameScreen.YOU != null) {
            if (GameScreen.YOU.getEntity() != null) {
                double[] world = GameScreen.YOU.getEntity().getWorldCoords();
                volume = 1-((float)MiscMath.distanceBetween(world[0], world[1], world_x, world_y)/1400);
            }
        }
        if (volume < 0.05) return;
        if (id < 0 || id >= ALL_SOUNDS.size()) return;
        Sound s = ALL_SOUNDS.get(id);
        if (s != null) {
            //if (s.playing() == false) {
                s.playAt(1f, volume, 0, 0, 1f);
            //}
        }
    }

}