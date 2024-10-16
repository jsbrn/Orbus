package network.server;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Server;
import gui.GameScreen;
import misc.Window;
import network.Packet.*;
import org.newdawn.slick.Color;
import world.PlayerCard;
import world.TeamCard;
import world.entities.Entity;
import world.entities.player.Player;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MPServer {
    
    static Server server;
    
    public MPServer(int port) {
        System.out.println("Launching server!");
        server = new Server(128*1024, 128*1024);
        registerPackets();
        server.addListener(new NetworkListener()); //add listener to poll for events like client disconnects
        try {
            server.bind(port, port); //bind server to port
            System.out.println("Bound to port: "+port);
        } catch (IOException ex) {
            System.err.println("Could not bind to port "+port+"!");
            Logger.getLogger(MPServer.class.getName()).log(Level.SEVERE, null, ex);
            server.stop();
            GameScreen.notification("Failed to start server: "+ex.getMessage().replace(": bind", ""), Color.red);
        }
        
        PlayerCard new_p = new PlayerCard("Player");
        new_p.setConnection(null);
        new_p.setHost(true);
        new_p.setBot(false);
        new_p.setTeam(GameScreen.AVAILABLE_TEAMS[0]);
        new_p.setWindowDimensions(Window.getWidth(), Window.getHeight());
        GameScreen.addPlayerCard(new_p);
        GameScreen.YOU = new_p;
        
        server.start();
        
    }
    
    private void registerPackets() {
        Kryo kryo = server.getKryo();
        
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
    
    public static void stop() {
        System.out.println("Stopping server...");
        if (server != null) {
            server.stop();
            server = null;
        }
    }
    
    public static boolean isRunning() {
        if (server != null) {
            return true;
        }
        return false;
    }
    
    
    //sends the object to all clients except the one specified as connection c
    public static void sendToAllOtherClients(Connection c, Object o, boolean force_send) {
        if (server == null) return;
        for (int i = 0; i != GameScreen.PLAYERS.size(); i++) {
            if (GameScreen.PLAYERS.get(i).equals(GameScreen.getPlayerCard(c))) continue;
            sendToPlayer(GameScreen.PLAYERS.get(i), o, force_send);
        }
    }
    
    //gets the position of the connection in the list of connections
    //-1 if connection does not exist in list
    public static int indexOfConnection(Connection c) {
        if (server == null) return -1;
        Connection[] list = MPServer.server.getConnections();
        for (int i = 0; i != list.length; i++) {
            if (list[i].isConnected()) {
                if (list[i].getID() == c.getID()) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public static void sendTeamMessage(String message, boolean urgent, TeamCard t, PlayerCard exception) {
        if (server == null) return;
        for (int i = 0; i != t.memberCount(); i++) {
            PlayerCard p = t.getMembers().get(i);
            if (!p.equals(exception)) {
                Notification n = new Notification();
                n.msg = message;
                n.urgent = urgent;
                if (p.isHost()) {
                    GameScreen.notification(n.msg, n.urgent);
                }
                if (p.getConnection() != null) {    
                    sendToPlayer(p, n, false);
                }
            }
        }
    }
    
    /**
     * Sends packet to the specified connected player. If the packet has to do with
     * updating entities/orbs, it will only send if the player will actually see the change.
     * @param p The PlayerCard instance.
     * @param o The packet.
     */
    public static void sendToPlayer(PlayerCard p, Object o, boolean force_send) {
        if (server == null || p == null) return;
        if (p.getConnection() == null) return;
        boolean send = false;
        if (o instanceof UpdateEntity || o instanceof UpdateMovementVector
                || o instanceof AddEntity || o instanceof RemoveEntity) {
            int index = -1;boolean orb = false;Entity e;
            if (o instanceof UpdateEntity) { 
                index = ((UpdateEntity)o).id; orb = ((UpdateEntity)o).orb; 
            }
            if (o instanceof AddEntity) { 
                index = ((AddEntity)o).id;orb = ((AddEntity)o).spawn_code.contains("orb");
            }
            if (o instanceof RemoveEntity) { 
                index = ((RemoveEntity)o).id;orb = ((RemoveEntity)o).orb;
            }
            if (o instanceof UpdateMovementVector) { 
                index = ((UpdateMovementVector)o).id;
                orb = ((UpdateMovementVector)o).orb;
            }
            if (orb) {
                e = GameScreen.getOrb(index);
            } else {
                e = GameScreen.getEntity(index);
            }
            if (e != null) {
                boolean friendly_player = (e instanceof Player && e.getTeam().getTeamCode() == p.getTeam().getTeamCode());
                
                if (p.canSee(e) || force_send) {
                    send = true;
                    System.out.println(o.getClass().getSimpleName()+" friendlyplayer = "+friendly_player);
                }
            } else {
                //System.out.println(o.getClass().getSimpleName()+" entity: "+e);
                send = true;
            }
        } else {
            send = true;
        }
        if (send) p.getConnection().sendTCP(o);
    }
    
    public static void sendMessageToAllPlayers(String message, boolean urgent) {
        if (server == null) return;
        Notification n = new Notification();
        n.msg = message;
        n.urgent = urgent;
        if (GameScreen.YOU.isHost()) GameScreen.notification(n.msg, n.urgent);
        sendToAllClients(n, false);
    }
    
    //sends object to all connected clients
    public static void sendToAllClients(Object o, boolean force_send) {
        if (server == null) return;
        for (int i = 0; i != GameScreen.PLAYERS.size(); i++) {
            sendToPlayer(GameScreen.PLAYERS.get(i), o, force_send);
        }
    }

}
