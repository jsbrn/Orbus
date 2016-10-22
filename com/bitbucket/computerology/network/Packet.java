package com.bitbucket.computerology.network;

public class Packet {

    /**
     * Host (aka server) sends this to all clients when a player is kicked.
     * The clients delete that player from the list.
     */
    public static class AddPlayer {
        public boolean you, host, bot;
        public String name;
        public int team_code;
    }
    
    /**
     * Host (aka server) sends this to all clients when a player is kicked.
     * The clients delete that player from the list.
     */
    public static class RemovePlayer {
        public int index;
    }
    
    /**
     * Currently not used.
     */
    public static class ReadyToggle {

    }
    
    public static class TeamToggle {
        public int index; //index of the playercard
    }
    
    /**
     * Client sends this to server and waits for confirmation.
     */
    public static class JoinRequest {
        public int update_id;
    }
    
    /**
     * Server sends this to client when client is safe to join.
     */
    public static class JoinConfirmation {
        public boolean confirmed;
        public String reason;
    }
    
    /**
     * Sent by server to all clients and tells them to open the GameScreen menu.
     * Once you add the setup code, send this packet after setup is complete.
     */
    public static class StartGame {}
    
    public static class AddEntity {
        public String spawn_code;
        public int team_code, id = -1;
        public double x, y, dx, dy, ax, ay;
    }
    
    public static class UpdateEntity {
        public int id, orbs, parts;
        public boolean orb;
        public double energy, x, y;
    }
    
    public static class RemoveEntity {
        public boolean orb;
        public int id;
    }
    
    public static class UpdatePlayerCard {
        public int entity_id, card_index, team_code;
    }
    
    public static class UpdateTeamCard {
        public int team_index, orbs, parts;
        public double score;
    }
    
    public static class UpdateMovementVector {
        public int id;
        public double x, y, dx, dy, ax, ay;
        public boolean orb = false;
    }
    
    public static class AddForce {
        public int index;
        public double x, y, dx, dy, ax, ay;
    }
    
    public static class RemoveForce {
        public int findex, eid;
        public double x, y;
    }
    
    public static class KeyPressed {
        public int key, p_index;
    }
    
    public static class KeyReleased {
        public int key, p_index;
    }
    
    public static class MousePressed {
        public int button;
        public double offset_x, offset_y;
    }
    
    public static class UpdatePowerupList {
        public int add_powerup_id = -1, remove_powerup_index = -1;
    }
    
    public static class Notification {
        public String msg;
        public boolean urgent;
    }
    
    public static class EndGame {
        public int winning_team;
    }
    
    public static class PlayerNameChange {
        public int player_index;
        public String new_name;
    }
    
    public static class ResetGame {}
    
    public static class AddAnimation {
        public int radius, type, team;
        public double x, y;
    }
    
    public static class WindowDimensions {
        public int w, h, player_index;
    }
    
}