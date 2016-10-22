package com.bitbucket.computerology.network.client;

import com.bitbucket.computerology.gui.GameScreen;
import static com.bitbucket.computerology.gui.GameScreen.WORLD_HEIGHT;
import static com.bitbucket.computerology.gui.GameScreen.WORLD_WIDTH;
import com.bitbucket.computerology.gui.LobbyMenu;
import com.bitbucket.computerology.gui.MainMenu;
import com.bitbucket.computerology.misc.Window;
import com.bitbucket.computerology.network.Packet.*;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.Force;
import com.bitbucket.computerology.world.PlayerCard;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.animations.Animation;
import com.bitbucket.computerology.world.entities.player.Player;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.LinkedList;
import org.newdawn.slick.Color;

public class MPClient {
    
    static Client client; //client instance
    NetworkListener listener;
    public static int PACKETS_RECIEVED = 0;
    public static LinkedList<Object> REMOVAL_PACKETS = new LinkedList<Object>(),
            ADDITION_PACKETS = new LinkedList<Object>(), UPDATE_PACKETS = new LinkedList<Object>(),
            MOVEMENT_UPDATE_PACKETS = new LinkedList<Object>();
    public static LinkedList<String> PACKET_NAMES = new LinkedList<String>();
    public static LinkedList<Integer> PACKET_COUNTS = new LinkedList<Integer>();
    
    public MPClient() {
        client = new Client(128*1024, 128*1024);
        registerPackets();
        listener = new NetworkListener();
        client.addListener(listener);
        new Thread(client).start();
    }
    
    public boolean connect(String ip, int port) {
        try {
            client.connect(5000, ip, port, port); //connect
            return true;
        } catch (Exception ex) {
            Logger.getLogger(MPClient.class.getName()).log(Level.SEVERE, null, ex);
            client.stop(); //stop if it fails
            MainMenu.enter();
            MainMenu.CONNECT_BUTTON.enable();
            GameScreen.notification("Failed to connect to "+ip, Color.red);
        }
        return false;
    }
    
    public static void disconnect() {
        client.close();
    }
    
    public boolean isConnected() {
        return client.isConnected();
    }
    
    public static ArrayList<String> getLANGames() {
        if (client == null) client = new Client(128*1024, 128*1024);
        List<InetAddress> server_list = client.discoverHosts(5757, 3000); //lan discovery
        ArrayList<String> ips = new ArrayList<String>();
        for (int i = 0; i != server_list.size(); i++) {
            InetAddress server = server_list.get(i);
            ips.add(server.getHostAddress());
        }
        return ips;
    }
    
    public static void processPacket(Connection c, Object o, boolean queue) {
        if (o instanceof JoinConfirmation) {
            JoinConfirmation jr = ((JoinConfirmation)o);
            if (!jr.confirmed) {
                MPClient.disconnect();
                GameScreen.notification(jr.reason, Color.lightGray);
            } else {
                WindowDimensions w = new WindowDimensions();
                w.w = Window.getWidth();
                w.h = Window.getHeight();
                MPClient.sendPacket(w);
            }
        } else if (o instanceof AddPlayer) {
            AddPlayer ap = ((AddPlayer)o);
            PlayerCard new_p = new PlayerCard("Player");
            new_p.setConnection(c);
            new_p.setTeam(GameScreen.AVAILABLE_TEAMS[ap.team_code]);
            new_p.setName(ap.name);
            new_p.setHost(ap.host);
            new_p.setBot(ap.bot);
            GameScreen.addPlayerCard(new_p);
            if (ap.you) { 
                GameScreen.YOU = new_p;
                GameScreen.YOU.setWindowDimensions(Window.getWidth(), Window.getHeight());
            }
        } else if (o instanceof RemovePlayer) {
            RemovePlayer rm = ((RemovePlayer)o);
            GameScreen.deletePlayerCard(rm.index);
        } else if (o instanceof AddEntity) {
            //if (((AddEntity)o).force_send) {
                AddEntity rm = ((AddEntity)o);
                Entity e = Entity.create(rm.spawn_code, rm.id);
                e.setTeam(GameScreen.AVAILABLE_TEAMS[rm.team_code]);
                e.setX(rm.x);
                e.setY(rm.y);
                e.setVelocity((int)rm.dx, (int)rm.dy, (int)rm.ax, (int)rm.ay, false);
                System.out.println("AddEntity packet: "+e.getSpawnCode()+" ["+e.getID()+"]");
                GameScreen.addEntity(e, false, false);
            //} else {
            //    ADDITION_PACKETS.add(o);
            //}
        } else if (o instanceof RemoveEntity) {
            REMOVAL_PACKETS.add(o);
        } else if (o instanceof TeamToggle) {
            GameScreen.PLAYERS.get(((TeamToggle)o).index).toggleTeam();
        } else if (o instanceof StartGame) {
            Camera.WORLD_X = (64*WORLD_WIDTH)/2;
            Camera.WORLD_Y = (64*WORLD_HEIGHT)/2;
            GameScreen.enter();
        } else if (o instanceof UpdatePlayerCard) {
            UpdatePlayerCard p = ((UpdatePlayerCard)o);
            PlayerCard pl = GameScreen.PLAYERS.get(p.card_index);
            pl.setEntity(((Player)GameScreen.getEntity(p.entity_id)));
            pl.setTeam(GameScreen.AVAILABLE_TEAMS[p.team_code]);
        } else if (o instanceof UpdateTeamCard) {
            UpdateTeamCard p = ((UpdateTeamCard)o);
            GameScreen.AVAILABLE_TEAMS[p.team_index].setPoints(p.score);
            GameScreen.AVAILABLE_TEAMS[p.team_index].setOrbCount(p.orbs);
            GameScreen.AVAILABLE_TEAMS[p.team_index].setPartsCount(p.parts);
        } else if (o instanceof UpdateEntity) {
            UPDATE_PACKETS.add(o);
        } else if (o instanceof UpdatePowerupList) {
            UpdatePowerupList p = ((UpdatePowerupList)o);
            GameScreen.YOU.addPowerup(p.add_powerup_id);
            GameScreen.YOU.removePowerup(p.remove_powerup_index);
        } else if (o instanceof UpdateMovementVector) {
            MOVEMENT_UPDATE_PACKETS.add(o);
        } else if (o instanceof AddForce) {
            AddForce p = ((AddForce)o);
            Entity e = GameScreen.GAME_OBJECTS.get(p.index);
            e.setX(p.x);
            e.setY(p.y);
            e.addForce(new Force((int)p.dx, (int)p.dy, (int)p.ax, (int)p.ay), false);
        } else if (o instanceof RemoveForce) {
            RemoveForce p = ((RemoveForce)o);
            Entity e = GameScreen.getEntity(p.eid);
            if (e != null) {
                e.setX(p.x);
                e.setY(p.y);
                e.removeForce(p.findex);
            }
        } else if (o instanceof Notification) {
            Notification n = ((Notification)o);
            System.out.println("Recieved message from server: "+n.msg);
            GameScreen.notification(n.msg, n.urgent);
        } else if (o instanceof EndGame) {
            int team = ((EndGame)o).winning_team;
            if (team > -1) {
                GameScreen.notification(GameScreen.AVAILABLE_TEAMS[team].getName()+" wins!", 
                        GameScreen.AVAILABLE_TEAMS[team].getTeamColor().brighter());
            } else {
                GameScreen.notification("Game ended by host.", 
                        Color.lightGray);
            }
            LobbyMenu.enter(true);
        } else if (o instanceof PlayerNameChange) {
            PlayerNameChange n = ((PlayerNameChange)o);
            PlayerCard p = GameScreen.PLAYERS.get(n.player_index);
            if (p != null) {
                p.setName(n.new_name);
            }
        } else if (o instanceof ResetGame) {
            GameScreen.resetGame();
        } else if (o instanceof KeyPressed) {
            PlayerCard p = GameScreen.PLAYERS.get(((KeyPressed)o).p_index);
            p.addPressedKey(((KeyPressed)o).key);
        } else if (o instanceof KeyReleased) {
            PlayerCard p = GameScreen.PLAYERS.get(((KeyReleased)o).p_index);
            p.removePressedKey(((KeyReleased)o).key);
        } else if (o instanceof AddAnimation) {
            AddAnimation aa = ((AddAnimation)o);
            Animation.create(aa.x, aa.y, aa.radius, aa.type, GameScreen.AVAILABLE_TEAMS[aa.team], false);
        }
    }
    
    public static void handleQueue() {
        try {
            for (int i = 0; i != ADDITION_PACKETS.size(); i++) {
                if (i >= ADDITION_PACKETS.size() || i < 0) break;
                AddEntity rm = ((AddEntity)ADDITION_PACKETS.get(i));
                Entity e = Entity.create(rm.spawn_code, rm.id);
                e.setTeam(GameScreen.AVAILABLE_TEAMS[rm.team_code]);
                e.setX(rm.x);
                e.setY(rm.y);
                e.setVelocity((int)rm.dx, (int)rm.dy, (int)rm.ax, (int)rm.ay, false);
                GameScreen.addEntity(e, false, false);
                ADDITION_PACKETS.remove(i);
                i--; if (i < 0) i = 0;
            }
            for (int i = 0; i != UPDATE_PACKETS.size(); i++) {
                if (i >= UPDATE_PACKETS.size() || i < 0) break;
                UpdateEntity rm = ((UpdateEntity)UPDATE_PACKETS.get(i));
                Entity e;
                if (rm.orb) {
                    e = GameScreen.getOrb(rm.id);
                } else {
                    e = GameScreen.getEntity(rm.id);
                }
                if (e != null) {
                    e.setEnergy(rm.energy);
                    e.setX(rm.x);
                    e.setY(rm.y);
                    e.setParts(rm.parts);
                    e.setOrbs(rm.orbs);
                    //System.out.println(e.getSpawnCode()+": "+rm.energy+", "+rm.x+", "+rm.y);
                    UPDATE_PACKETS.remove(i);
                    i--; if (i < 0) i = 0;
                }
            }
            for (int i = 0; i != MOVEMENT_UPDATE_PACKETS.size(); i++) {
                if (i >= MOVEMENT_UPDATE_PACKETS.size() || i < 0) break;
                UpdateMovementVector p = ((UpdateMovementVector)MOVEMENT_UPDATE_PACKETS.get(i));
                Entity e = null;
                if (p.orb) {
                    e = GameScreen.getOrb(p.id);
                } else {
                    e = GameScreen.getEntity(p.id);
                }
                if (e != null) {
                    //if (e.equals(GameScreen.YOU.getEntity())) return;
                    e.setX(p.x);
                    e.setY(p.y);
                    e.setVelocity((int)p.dx, (int)p.dy, (int)p.ax, (int)p.ay, false);
                    MOVEMENT_UPDATE_PACKETS.remove(i);
                    i--; if (i < 0) i = 0;
                }
                
            }
            for (int i = 0; i != REMOVAL_PACKETS.size(); i++) {
                if (i >= REMOVAL_PACKETS.size() || i < 0) break;
                RemoveEntity rm = ((RemoveEntity)REMOVAL_PACKETS.get(i));
                Entity e;
                if (rm.orb) {
                    e = GameScreen.getOrb(rm.id);
                } else {
                    e = GameScreen.getEntity(rm.id);
                }
                if (e != null) {
                    GameScreen.deleteEntity(e, false);
                    REMOVAL_PACKETS.remove(i);
                    i--; if (i < 0) i = 0;
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void registerPackets() {
        Kryo kryo = client.getKryo();
        
        kryo.register(ReadyToggle.class);
        kryo.register(JoinRequest.class);
        kryo.register(JoinConfirmation.class);
        kryo.register(AddPlayer.class);
        kryo.register(RemovePlayer.class);
        kryo.register(StartGame.class);
        kryo.register(TeamToggle.class);
        kryo.register(AddEntity.class);
        kryo.register(RemoveEntity.class);
        kryo.register(UpdateEntity.class);
        kryo.register(UpdatePlayerCard.class);
        kryo.register(UpdateTeamCard.class);
        kryo.register(UpdatePowerupList.class);
        kryo.register(UpdateMovementVector.class);
        kryo.register(AddForce.class);
        kryo.register(RemoveForce.class);
        kryo.register(MousePressed.class);
        kryo.register(KeyPressed.class);
        kryo.register(KeyReleased.class);
        kryo.register(Notification.class);
        kryo.register(EndGame.class);
        kryo.register(PlayerNameChange.class);
        kryo.register(ResetGame.class);
        kryo.register(AddAnimation.class);        
        kryo.register(WindowDimensions.class);   
    }
    
    public static void sendPacket(Object p) {
        if (client == null) return;
        System.out.print(""); //for some reason it doesn't work unless this is here
        client.sendTCP(p);
        
    }
    
}
