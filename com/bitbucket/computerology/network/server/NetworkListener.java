package com.bitbucket.computerology.network.server;

import com.bitbucket.computerology.gui.GameScreen;
import static com.bitbucket.computerology.gui.GameScreen.YOU;
import static com.bitbucket.computerology.gui.GameScreen.addEntity;
import com.bitbucket.computerology.main.SlickInitializer;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.network.Packet.*;
import com.bitbucket.computerology.world.PlayerCard;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.powerups.PowerUp;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import org.newdawn.slick.Input;

public class NetworkListener extends Listener {
    
    @Override
    public void connected(Connection c) {
        PlayerCard new_p = new PlayerCard("Player");
        new_p.setConnection(c);
        new_p.setTeam(GameScreen.AVAILABLE_TEAMS[0]);
        GameScreen.addPlayerCard(new_p);
        for (int i = 0; i != GameScreen.PLAYERS.size(); i++) {
            AddPlayer ap = new AddPlayer();
            ap.name = GameScreen.PLAYERS.get(i).getName();
            ap.you = false;
            ap.host = GameScreen.PLAYERS.get(i).isHost();
            ap.bot = GameScreen.PLAYERS.get(i).isBot();
            ap.team_code = GameScreen.PLAYERS.get(i).getTeam().getTeamCode();
            if (i == GameScreen.PLAYERS.size()-1) ap.you = true;
            c.sendTCP(ap);
        }
        AddPlayer ap = new AddPlayer();
        ap.name = new_p.getName();
        ap.you = false;
        ap.host = new_p.isHost();
        ap.bot = new_p.isBot();
        ap.team_code = new_p.getTeam().getTeamCode();
        MPServer.sendToAllOtherClients(c, ap, false);
    }

    @Override
    public void disconnected(Connection c) {
        int index = GameScreen.PLAYERS.indexOf(GameScreen.getPlayerCard(c));
        if (index > -1) {
            GameScreen.deletePlayerCard(index);
        }
    }

    @Override
    public void received(Connection c, Object o) {
        PlayerCard pl = GameScreen.getPlayerCard(c);
        if (o instanceof JoinRequest) {
            JoinRequest jr = ((JoinRequest)o);
            JoinConfirmation jc = new JoinConfirmation();
            jc.confirmed = (jr.update_id == SlickInitializer.UPDATE_ID 
                    && !GameScreen.isCurrentState() && GameScreen.PLAYERS.size() < 16);
            if (GameScreen.PLAYERS.size() >= 16) {
                jc.reason = "Server is full!";
            }
            if (GameScreen.isCurrentState()) {
                jc.reason = "This game is already in session!";
            }
            if (jr.update_id != SlickInitializer.UPDATE_ID) {
                jc.reason = "Server is running a";
                if (jr.update_id < SlickInitializer.UPDATE_ID) {
                    jc.reason+=" newer version!";
                } else {
                    jc.reason+="n older version!";
                }
            }
            c.sendTCP(jc);
        }
        if (o instanceof TeamToggle) {
            GameScreen.getPlayerCard(c).toggleTeam();
            MPServer.sendToAllClients(o, false);
        }
        if (o instanceof MousePressed) {
            PlayerCard p = GameScreen.getPlayerCard(c);
            Entity e = p.getEntity();
            if (e == null) return;
            MousePressed m = ((MousePressed)o);
            if (m.button == 0) {
                if ((int)e.getEnergy() > 10) {
                    e.addEnergy(-5);
                    //spawn a new orb
                    Entity pj = Entity.create("projectile", -1);
                    //set x to outside the circle facing the entity
                    double point[] = e.getRotatedPoint(0, e.getSize() + 20, (int)MiscMath.angleBetween
                (0, 0, -m.offset_x, -m.offset_y));
                    pj.setX(point[0]);
                    pj.setY(point[1]);
                    pj.setTeam(e.getTeam());
                    pj.setVelocityFor(e.getWorldCoords()[0]-m.offset_x, e.getWorldCoords()[1]-m.offset_y, 0, 0, 250, false);
                    GameScreen.addEntity(pj, false, false);
                }
            }
            if (m.button == 1) {
                if ((int)e.getEnergy() > 20) {
                    e.addEnergy(-15);
                    //spawn a new orb
                    Entity pj = Entity.create("energyorb", -1);
                    //set x to outside the circle facing the entity
                    double point[] = e.getRotatedPoint(0, e.getSize() + 20, (int)MiscMath.angleBetween
                (0, 0, -m.offset_x, -m.offset_y));
                    pj.setX(point[0]);
                    pj.setY(point[1]);
                    pj.setTeam(e.getTeam());
                    pj.setVelocityFor(e.getWorldCoords()[0]-m.offset_x, e.getWorldCoords()[1]-m.offset_y, -1, -1, 350, false);
                    GameScreen.addEntity(pj, false, false);
                }
            }
        }
        if (o instanceof KeyPressed) {
            PlayerCard p = GameScreen.getPlayerCard(c);
            int key = ((KeyPressed)o).key;
            p.addPressedKey(key);
            //powerups 
            int hotkey_clicked = key-2;
            p.spawnPowerup(hotkey_clicked);
            //movement
            double dx = 0, dy = 0;
            double m = 1;
            if (p.isKeyPressed(Input.KEY_LSHIFT)) m = 2;
            if (p.isKeyPressed(Input.KEY_W)) dy -= 100*m;
            if (p.isKeyPressed(Input.KEY_A)) dx -= 100*m;
            if (p.isKeyPressed(Input.KEY_S)) dy += 100*m;
            if (p.isKeyPressed(Input.KEY_D)) dx += 100*m;
            if ((dx < 0 && dy < 0) || (dx > 0 && dy < 0) 
                    || (dx > 0 && dy > 0) || (dx < 0 && dy > 0)) {
                dx*=0.7;dy*=0.7;
            }
            p.getEntity().setVelocity(dx, dy, 0, 0, true);
            MPServer.sendToAllOtherClients(c, o, false);
        }
        if (o instanceof KeyReleased) {
            PlayerCard p = GameScreen.getPlayerCard(c);
            int key = ((KeyReleased)o).key;
            p.removePressedKey(key);
            double dx = 0, dy = 0;
            double m = 1;
            if (p.isKeyPressed(Input.KEY_LSHIFT)) m = 2;
            if (p.isKeyPressed(Input.KEY_W)) dy -= 100*m;
            if (p.isKeyPressed(Input.KEY_A)) dx -= 100*m;
            if (p.isKeyPressed(Input.KEY_S)) dy += 100*m;
            if (p.isKeyPressed(Input.KEY_D)) dx += 100*m;
            if ((dx < 0 && dy < 0) || (dx > 0 && dy < 0) 
                    || (dx > 0 && dy > 0) || (dx < 0 && dy > 0)) {
                dx*=0.7;dy*=0.7;
            }
            p.getEntity().setVelocity(dx, dy, 0, 0, true);
            MPServer.sendToAllOtherClients(c, o, false);
        }
        if (o instanceof PlayerNameChange) {
            PlayerNameChange n = ((PlayerNameChange)o);
            PlayerCard p = GameScreen.PLAYERS.get(n.player_index);
            if (p != null) {
                p.setName(n.new_name);
            }
            MPServer.sendToAllClients(o, false);
        }
        if (o instanceof WindowDimensions) {
            WindowDimensions wd = ((WindowDimensions)o);
            PlayerCard p = GameScreen.PLAYERS.get(wd.player_index);
            if (p != null) {
                System.out.println(p.getName()+" changed window dimensions to "+wd.w+", "+wd.h);
                p.setWindowDimensions(wd.w, wd.h);
            }
        }
    }
}