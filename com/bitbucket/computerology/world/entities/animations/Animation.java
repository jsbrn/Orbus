package com.bitbucket.computerology.world.entities.animations;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.world.TeamCard;
import com.bitbucket.computerology.world.entities.Entity;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;

public class Animation extends Entity {
    
    public static int RADIATE_OUTWARD = 0, RADIATE_INWARD;
    int type = -1;
    float r = 0;
    Entity entity;
    
    /**
     * Create an animation and add it to the world.
     * @param x World coordinates.
     * @param y World coordinates.
     * @param radius Radius of the animation.
     * @param type What animation? Ex. Animation.RADIATE_OUTWARD
     * @return 
     */
    public static Animation create(double x, double y, int radius, int type, 
            TeamCard team, boolean send_packet) {
        Animation a = new Animation();
        a.setSize(radius);
        a.type = type;
        a.r = radius;
        a.entity = null;
        a.setX(x);
        a.setY(y);
        a.setTeam(team);
        GameScreen.addAnimation(a, send_packet);
        return a;
    }
    
    public static Animation create(Entity e, int radius, int type, 
            TeamCard team, boolean send_packet) {
        Animation a = Animation.create(e.getWorldCoords()[0], e.getWorldCoords()[1], 
                radius, type, team, send_packet);
        a.entity = e;
        return a;
    }

    public Animation() {
        
    }
    
    public int getType() {
        return type;
    }
    
    public void draw(Graphics g) {
        if (entity != null) {
            this.setX(entity.getWorldCoords()[0]);
            this.setY(entity.getWorldCoords()[1]);
        }
        float render_r = 0, alpha = 1;
        r -= MiscMath.getConstant(300, 1);
        alpha -= MiscMath.getConstant(1, 1);
        if (type == RADIATE_INWARD) {
            render_r = r;
        } else if (type == RADIATE_OUTWARD) {
            render_r = getSize()-r;
        }
        if (r > 0) {
            g.setLineWidth(2);
            g.setColor(getTeam().getTeamColor().addToCopy(new Color(0, 0, 0, alpha)));
            if (getTeam().getTeamCode() == TeamCard.NEUTRAL_TEAM) {
                g.setColor(Color.black);
            }
            g.drawOval((float)getOnscreenCoords()[0]-render_r, 
                    (float)getOnscreenCoords()[1]-render_r, render_r*2, render_r*2);
        } else {
            GameScreen.ANIMATIONS.remove(this);
        }
        g.setLineWidth(1);
    }
}
