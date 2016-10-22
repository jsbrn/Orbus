package com.bitbucket.computerology.world;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.gui.LobbyMenu;
import com.bitbucket.computerology.network.Packet.EndGame;
import com.bitbucket.computerology.network.Packet.UpdateTeamCard;
import com.bitbucket.computerology.network.server.MPServer;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.homebase.HomeBase;
import java.util.LinkedList;
import org.newdawn.slick.Color;

/**
 * The class representing the player with all it's stats and whatnot.
 */
public class TeamCard {
    
    public static int RED_TEAM = 0, BLUE_TEAM = 1, GREEN_TEAM = 2, PURPLE_TEAM = 3, NEUTRAL_TEAM = 4;
    public static Color RED = new Color(200, 50, 50);
    public static Color BLUE = new Color(50, 75, 200);
    public static Color GREEN = new Color(64, 200, 50);
    public static Color PURPLE = new Color(180, 0, 255);

    double score, score_at_last_packet;
    String name;
    int team = 0, orbs = 0, parts = 0;
    
    HomeBase home;
    
    LinkedList<PlayerCard> members;
    LinkedList<Entity> entities;
    
    public TeamCard(String name, int team_code) {
        this.name = name;
        this.team = team_code;
        this.score = 0;
        this.parts = 0;
        this.score_at_last_packet = 0;
        this.members = new LinkedList<PlayerCard>();
        this.entities = new LinkedList<Entity>();
    }
    
    public LinkedList<PlayerCard> getMembers() {
        return members;
    }
    
    public String getName() {
        return name;
    }
    
    public void addMember(PlayerCard p) {
        System.out.println(p.getName()+" added to "+getName()+" members!");
        members.add(p);
    }
    
    public void removeMember(PlayerCard p) {
        System.out.println(p.getName()+" removed from "+getName()+" members!");
        members.remove(p);
    }
    
    public void addEntity(Entity p) {
        entities.add(p);
    }
    
    public void setPoints(double d) {
        score = d;
    }
    
    public void removeEntity(Entity p) {
        entities.remove(p);
    }
    
    public void addOrb() {
        orbs++;
        if (GameScreen.YOU.isHost()) {
            UpdateTeamCard u = new UpdateTeamCard();
            u.score = score;
            u.team_index = team;
            u.orbs = orbs;
            MPServer.sendToAllClients(u, false);
        }
    }
    
    public void removeOrb() {
        orbs--;
        if (GameScreen.YOU.isHost()) {
            UpdateTeamCard u = new UpdateTeamCard();
            u.score = score;
            u.team_index = team;
            u.orbs = orbs;
            MPServer.sendToAllClients(u, false);
        }
    }
    
    public void addPart() {
        parts++;
        if (GameScreen.YOU.isHost()) {
            UpdateTeamCard u = new UpdateTeamCard();
            u.score = score;
            u.team_index = team;
            u.orbs = orbs;
            u.parts = parts;
            MPServer.sendToAllClients(u, false);
            String prefix = "A Circuit";
            String suffix = " has been added to our Home Base!";
            String secondary = "We can now start collecting orbs!";
            if (getPartCount() >= GameScreen.getActiveTeams().size()) {
                prefix = "The final Circuit";
            } else {
                secondary = "We have "+getPartCount()+" out of "+GameScreen.getActiveTeams().size()+" Circuits!";
            }
            MPServer.sendTeamMessage(prefix+suffix, false, this, null);
            MPServer.sendTeamMessage(secondary, false, this, null);
        }
    }
    
    public void removePart() {
        parts--;
        if (GameScreen.YOU.isHost()) {
            UpdateTeamCard u = new UpdateTeamCard();
            u.score = score;
            u.team_index = team;
            u.orbs = orbs;
            u.parts = parts;
            MPServer.sendToAllClients(u, false);
            String prefix = "A Circuit";
            String suffix = " has been stolen from our Home Base!";
            String secondary = "We need those damn parts lol top kek";
            if (getPartCount() >= GameScreen.getActiveTeams().size()-1) {
                prefix = "A Circuit";
                secondary = "Our Home Base is not collecting orbs!";
            } else {
                secondary = "We have "+getPartCount()+" out of "+GameScreen.getActiveTeams().size()+" Circuits!";
            }
            MPServer.sendTeamMessage(prefix+suffix, true, this, null);
            MPServer.sendTeamMessage(secondary, true, this, null);
        }
    }
    
    public int getOrbCount() {
        return orbs;
    }
    
    public int getPartCount() {
        return parts;
    }
    
    public void setOrbCount(int o) {
        orbs = o;
    }
    
    public void setPartsCount(int o) {
        parts = o;
    }
    
    public void removeMember(int index) {
        removeMember(members.get(index));
    }
    
    public int getTeamCode() {
        return team;
    }
    
    public void setTeamCode(int t) {
        team = t;
    }
    
    public Color getTeamColor() {
        if (team == RED_TEAM) return RED;
        if (team == BLUE_TEAM) return BLUE;
        if (team == GREEN_TEAM) return GREEN;
        if (team == PURPLE_TEAM) return PURPLE;
        return Color.white;
    }
    
    public int getPoints() {
        return (int)score;
    }
    
    public void setHomeBase(HomeBase hb) {
        home = hb;
    }
    
    public HomeBase getHomeBase() {
        return home;
    }
    
    public int memberCount() {
        return members.size();
    }
    
    public void reset() {
        System.out.println("Resetting "+getName()+" to default!");
        members.clear();
        score = 0;
        entities.clear();
        orbs = 0;
        parts = 1;
        home = null;
    }
    
    public void addScore(double amount) {
        if (Math.abs(score - score_at_last_packet) > 5 && GameScreen.YOU.isHost()) {
            UpdateTeamCard u = new UpdateTeamCard();
            u.score = score+amount;
            u.team_index = team;
            u.orbs = orbs;
            MPServer.sendToAllClients(u, false);
            score_at_last_packet = score;
        }
        score+=amount;
        if (score < 0) score = 0;
        if (score >= GameScreen.MAX_POINTS && GameScreen.YOU.isHost()) {
            EndGame u = new EndGame();
            u.winning_team = team;
            if (team > -1) {
                GameScreen.notification(GameScreen.AVAILABLE_TEAMS[team].getName()+" wins!", 
                        GameScreen.AVAILABLE_TEAMS[team].getTeamColor().brighter());
            } else {
                GameScreen.notification("Game ended by host.", 
                        Color.lightGray);
            }
            MPServer.sendToAllClients(u, false);
            LobbyMenu.enter(true);
        }
    }
    
}
