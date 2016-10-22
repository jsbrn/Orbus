package com.bitbucket.computerology.gui;

import com.bitbucket.computerology.main.SlickInitializer;
import com.bitbucket.computerology.misc.Window;

import org.lwjgl.input.Mouse;
import org.newdawn.slick.state.*;

import com.bitbucket.computerology.gui.elements.*;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.network.Packet.*;
import com.bitbucket.computerology.network.client.MPClient;
import com.bitbucket.computerology.network.server.MPServer;
import com.bitbucket.computerology.world.Camera;
import com.bitbucket.computerology.world.PlayerCard;
import com.bitbucket.computerology.world.TeamCard;
import com.bitbucket.computerology.world.entities.Entity;
import com.bitbucket.computerology.world.entities.animations.Animation;
import com.bitbucket.computerology.world.entities.orbs.Orb;
import com.bitbucket.computerology.world.entities.homebase.HomeBase;
import com.bitbucket.computerology.world.entities.player.Player;
import com.bitbucket.computerology.world.entities.powerups.PowerUp;
import com.esotericsoftware.kryonet.Connection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;

public class GameScreen extends BasicGameState {

    static Input input;
    static StateBasedGame game_state;
    public static double DELTA_TIME = 1;
    public static LinkedList<Entity> GAME_OBJECTS;
    public static LinkedList<Orb> ORBS;
    public static LinkedList<PlayerCard> PLAYERS;
    public static LinkedList<Animation> ANIMATIONS;
    public static PlayerCard YOU;
    
    public static boolean SHOW_HELP = false, DEBUG_MODE = false;
    static ListDisplay controls_list;
    Button leave, end;
    
    static int orb_update_group = 0;
    public static LinkedList<TextDisplay> TEXT_POPUPS;
    public static int PING = 0, MAX_POINTS = 2500;
    public static TeamCard[] AVAILABLE_TEAMS;
    public static int WORLD_WIDTH = 64, WORLD_HEIGHT = 64;
    
    static float help_offset_y = 1000;
    
    Image help, victory_progress, hud, partpanelside, partpaneltop, circuit, tip_orbs, tip_circuits;

    public GameScreen(int state) {

    }

    @Override
    public int getID() {
        return 0;
    }
    
    public static StateBasedGame getStateBasedGame() {
        return game_state;
    }
    
    /**
     * Displays text on the screen.
     * @param x
     * @param y
     * @param duration
     * @param text The text to display.
     * @param c The color of the text.
     */
    public static void displayText(double x, double y, String text, Color c) {
        TextDisplay new_ = new TextDisplay(1000);
        if (c == null) c = Color.white;
        new_.setX(x);
        new_.setY(y);
        new_.setBackgroundColor(new Color(0, 0, 0, 0));
        new_.setTextColor(c);
        new_.addText(text);
        new_.fly(false, -0.4f);
        TEXT_POPUPS.add(new_);
    }
    
    public static void notification(String text, Color c) {
        TextDisplay new_ = new TextDisplay(1000);
        if (c == null) c = Color.white;
        new_.setX(Window.getWidth()/2 - SlickInitializer.NORMAL_FONT.getWidth(text)/2);
        new_.setY(150);
        if (!TEXT_POPUPS.isEmpty()) new_.setY(TEXT_POPUPS.getLast().getY() + 25);
        new_.setBackgroundColor(new Color(0, 0, 0, 0));
        new_.setTextColor(c);
        new_.addText(text);
        new_.fly(false, -0.2f);
        TEXT_POPUPS.add(new_);
    }
    
    public static void notification(String text, boolean urgent) {
        if (urgent) {
            notification(text, Color.red);
        } else {
            notification(text, Color.lightGray);
        }
    }

    @Override
    public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
        game_state = sbg;
        leave = new Button("Disconnect");
        end = new Button("End Game");
        help = new Image("resources/images/help.png", false, Image.FILTER_LINEAR);
        victory_progress = new Image("resources/images/progress.png", false, Image.FILTER_LINEAR);
        partpanelside = new Image("resources/images/partpanelside.png", false, Image.FILTER_LINEAR);
        partpaneltop = new Image("resources/images/partpaneltop.png", false, Image.FILTER_LINEAR);
        circuit = new Image("resources/images/circuit.png", false, Image.FILTER_LINEAR);
        hud = new Image("resources/images/playerhud.png", false, Image.FILTER_LINEAR);
        tip_orbs = new Image("resources/images/tip_orbs.png", false, Image.FILTER_LINEAR);
        tip_circuits = new Image("resources/images/tip_circuits.png", false, Image.FILTER_LINEAR);
        AVAILABLE_TEAMS = new TeamCard[]{
            new TeamCard("Red Team", TeamCard.RED_TEAM),
            new TeamCard("Blue Team", TeamCard.BLUE_TEAM),
            new TeamCard("Green Team", TeamCard.GREEN_TEAM),
            new TeamCard("Purple Team", TeamCard.PURPLE_TEAM),
            new TeamCard("Neutral Team", TeamCard.NEUTRAL_TEAM)
        };
        TEXT_POPUPS = new LinkedList<TextDisplay>();
        GAME_OBJECTS = new LinkedList<Entity>();
        ORBS = new LinkedList<Orb>();
        ANIMATIONS = new LinkedList<Animation>();
        
        controls_list = new ListDisplay(200);
        controls_list.setTitle("Controls");
        controls_list.setTitleColor(Color.yellow);
        controls_list.setTextColor(Color.white);
        controls_list.setBackgroundColor(new Color(0, 0, 0, 100));
        
        PLAYERS = new LinkedList<PlayerCard>();
        input = gc.getInput();
    }

    //draws state (screen) elements
    @Override
    public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
        gc.getInput().disableKeyRepeat();
        if (YOU == null) return;
    	int mouse_x = Mouse.getX();
    	int mouse_y = Window.getHeight() - Mouse.getY();
        
        renderWorld(g);
        
        controls_list.clearText();
        controls_list.setTitleColor(Color.yellow);
        controls_list.setX(Window.getWidth() - controls_list.getWidth() - 5);
        controls_list.setY(Window.getHeight() - 45 - controls_list.getHeight());
        controls_list.addLine("Move: WASD");
        controls_list.addLine("Sprint: hold shift");
        controls_list.addLine("Shoot: left mouse");
        controls_list.addLine("Give energy: right mouse");
        controls_list.addLine("Powerups: 1-4");
        controls_list.draw(g);
        if (SHOW_HELP)
            controls_list.hide();
        else
            controls_list.show();
        
        g.setColor(Color.black);
        
        
        
        for (int i = 0; i != GameScreen.TEXT_POPUPS.size(); i++) {
            if (i < GameScreen.TEXT_POPUPS.size()) {
                TextDisplay t = GameScreen.TEXT_POPUPS.get(i);
                t.update();
                t.draw(g);
                if (!t.isVisible()) { TEXT_POPUPS.remove(t); break; }
            }
        }
        
        if (input.isKeyPressed(Input.KEY_L)) {
            Entity e = Entity.create("part", -1);
            e.setX(YOU.getEntity().getWorldCoords()[0]);
            e.setY(YOU.getEntity().getWorldCoords()[1]);
            GameScreen.addEntity(e, false, false);
        }
        
        leave.setX(Window.getWidth() - leave.getWidth() - 5);
        leave.setY(Window.getHeight() - leave.getHeight() - 5);
        leave.draw(g);
        
        end.setX(Window.getWidth()-leave.getWidth()-end.getWidth()- 10);
        end.setY(Window.getHeight() - end.getHeight() - 5);
        end.draw(g);
        
        if (SHOW_HELP) {
            g.setColor(new Color(0, 0, 0, 100));
            g.fillRect(0, 0, Window.getWidth(), Window.getHeight());
        } else {
            g.drawImage(victory_progress, Window.getWidth()/2-victory_progress.getWidth()/2, 10);
            int teams = 0;
            for (TeamCard t: AVAILABLE_TEAMS) {
                if (t.memberCount() > 0
                        && t.getTeamCode() != TeamCard.NEUTRAL_TEAM) teams++;
            }
            float total_width = 474;
            float team_width = total_width/teams;
            for (int i = 0; i != teams; i++) {
                TeamCard t = AVAILABLE_TEAMS[i];
                float t_progress = t.getPoints();
                float visual_width = (team_width/MAX_POINTS)*t_progress;
                float x = Window.getWidth()/2-victory_progress.getWidth()/2 + 12 + (team_width*i);
                g.setColor(t.getTeamColor().addToCopy(new Color(0, 0, 0, -150)).brighter());
                g.fillRect(x, 10+25, visual_width, 14);
                g.setColor(Color.lightGray);
                g.setLineWidth(2);
                g.fillRect(x + visual_width, 10+25, team_width-visual_width, 14);
                g.setColor(Color.black);
                if (i < teams-1) g.drawLine(x+team_width, 10+25, x+team_width, 10+25+14);
                g.setLineWidth(1);
            }

            int toppanelheight = 0;
            for (int i = 1; i != 1+(int)(GameScreen.getActiveTeams().size()*1.5); i++) {
                g.drawImage(partpanelside, 0,Window.getHeight()-hud.getHeight()-(i*partpanelside.getHeight()));
                toppanelheight-=partpanelside.getHeight();
            }
            int chip_x = 4, chip_y = 5+Window.getHeight()-hud.getHeight()+toppanelheight;
            for (int i = 0; i != GameScreen.getActiveTeams().size(); i++) {
                int chips = GameScreen.getActiveTeams().get(i).getPartCount();
                for (int j = 0; j != GameScreen.getActiveTeams().size(); j++) {
                    g.setColor(Color.black);
                    g.drawRect(chip_x-2, chip_y-2, circuit.getWidth()+4, circuit.getHeight()+4);
                    g.setColor(GameScreen.getActiveTeams().get(i).getTeamColor());
                    g.drawRect(chip_x-1, chip_y-1, circuit.getWidth()+2, circuit.getHeight()+2);
                    if (j < chips) g.drawImage(circuit, chip_x, chip_y);
                    chip_x += 26;
                }
                chip_x = 4;
                chip_y += 26;
            }
            g.drawImage(partpaneltop, 0,Window.getHeight()-hud.getHeight()+toppanelheight-partpaneltop.getHeight());
            g.drawImage(hud, 0, Window.getHeight()-hud.getHeight());
            g.setColor(GameScreen.YOU.getTeam().getTeamColor().addToCopy(new Color(0, 0, 0, -150)).brighter());
            float height = 123;
            float hp = GameScreen.YOU.getEnergy();
            if (hp >= 100) hp = 100;
            float hp_height = (height/100)*hp;
            g.fillRect(10, Window.getHeight()-hud.getHeight()+16 + (height-hp_height), 22, hp_height);
            g.setColor(Color.lightGray);
            g.fillRect(10, Window.getHeight()-hud.getHeight()+16, 22, height-hp_height);
            int start_x = 194, start_y = Window.getHeight()-hud.getHeight()+96;
            for (int i = 0; i != 4; i++) {
                int id = GameScreen.YOU.getPowerup(i);
                if (id <= 0 || id >= PowerUp.ICONS.length) continue;
                Image icon = PowerUp.ICONS[id];
                g.drawImage(icon, start_x+(i*52)-icon.getWidth()/2, start_y-icon.getHeight()/2);
            }
            int energy_val = GameScreen.YOU.getEnergy();
            if (energy_val < 5) energy_val = 5;
            if (energy_val > 123 && energy_val < 126) energy_val = 125;
            String energy = energy_val+"%";
            Color energy_color = Color.green;
            if (energy_val < 50) {
                energy_color = Color.red.brighter();
            } else if (energy_val < 75) {
                energy_color = Color.yellow;
            } else if (energy_val > 100) {
                energy_color = Color.green;
            }
            g.setFont(SlickInitializer.NORMAL_FONT);
            g.setColor(energy_color);
            g.drawString(energy, 60, Window.getHeight()-hud.getHeight()+42);
            g.setColor(Color.white);
            g.drawString(GameScreen.YOU.getEntity().getOrbs()+" orbs", 60, Window.getHeight()-hud.getHeight()+82);
            g.drawString(GameScreen.YOU.getEntity().getParts()+" circuits", 60, Window.getHeight()-hud.getHeight()+102);
            
            if (YOU.getTeam().getPartCount() >= getActiveTeams().size()) {
                g.drawImage(tip_orbs, 10, 10);
            } else {
                g.drawImage(tip_circuits, 10, 10);
            }
            
        }
        
        g.drawImage(help, Window.getWidth()/2-help.getWidth()/2, 
                help_offset_y + Window.getHeight()/2-help.getHeight()/2);
        if (DEBUG_MODE) {
            g.setColor(Color.black);
            g.drawString("FPS: "+gc.getFPS(), 5, 5);
            for (int i = 0; i != GAME_OBJECTS.size(); i++) {
                double[] world = GAME_OBJECTS.get(i).getOnscreenCoords();
                int[] render = GAME_OBJECTS.get(i).getRenderCoords();
                g.setLineWidth(4);
                g.setColor(GAME_OBJECTS.get(i).getTeam().getTeamColor());
                g.drawLine((float)world[0], (float)world[1], (float)world[0], (float)world[1]);
                g.setLineWidth(1);
                g.drawOval(render[0], render[1], GAME_OBJECTS.get(i).getSize()*2, GAME_OBJECTS.get(i).getSize()*2);
            }
            if (YOU.isHost() == false) {
                g.setColor(Color.black);
                g.drawString("Packets recieved: "+MPClient.PACKETS_RECIEVED, 5, 25);
                g.drawString("Entity count: "+(GAME_OBJECTS.size()+ORBS.size()), 5, 45);
            }
            g.drawString("FPS: "+gc.getFPS(), (int)controls_list.getX(), (int)controls_list.getY() - 25);
        }

    }

    //key binding and calling update() in all objects
    @Override
    public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
        
        if (SHOW_HELP) help_offset_y -= MiscMath.getConstant(2400, 1); 
        else help_offset_y += MiscMath.getConstant(2400, 1);
        if (help_offset_y > Window.getHeight()) help_offset_y = Window.getHeight();
        if (help_offset_y < 0) help_offset_y = 0;
        
        if (Window.wasResized()) {
            if (!GameScreen.YOU.isHost()) {
                WindowDimensions w = new WindowDimensions();
                w.w = Window.getWidth();
                w.h = Window.getHeight();
                w.player_index = GameScreen.PLAYERS.indexOf(GameScreen.YOU);
                MPClient.sendPacket(w);
                GameScreen.YOU.setWindowDimensions(Window.getWidth(), Window.getHeight());
            } else {
                GameScreen.YOU.setWindowDimensions(Window.getWidth(), Window.getHeight());
            }
        }
        
        if (input.isKeyPressed(Input.KEY_P)) {
            for (int i = 0; i != MPClient.PACKET_NAMES.size(); i++) {
                System.out.println(MPClient.PACKET_NAMES.get(i)+": "+MPClient.PACKET_COUNTS.get(i));
            }
        }
        if (input.isKeyPressed(Input.KEY_SLASH)) {
            DEBUG_MODE = !DEBUG_MODE;
        }
        if (input.isKeyPressed(Input.KEY_E)) {
            for (int i = 0; i != GAME_OBJECTS.size(); i++) {
                Entity e = GAME_OBJECTS.get(i);
                System.out.println(e.getSpawnCode()+" at "+e.getOnscreenCoords()[0]+", "+e.getOnscreenCoords()[1]);
            }
        }
        
        game_state = sbg;
        DELTA_TIME = delta;
        
        end.hide();
        if (GameScreen.YOU.isHost()) end.show();
        if (end.isClicked(0)) {
            EndGame e = new EndGame();
            e.winning_team = -1;
            MPServer.sendToAllClients(e, false);
            LobbyMenu.enter(true);
        }
        
        if (YOU == null) return;
        Camera.setTarget(YOU.getEntity());
        
        /*
        Update/move all entities and orbs. Only update if you are host.
        */
        try {
            LinkedList<Entity> all_entities = new LinkedList<Entity>();
            all_entities.addAll(GAME_OBJECTS);
            for (int i = 0; i != all_entities.size(); i++) {
                if (i >= all_entities.size() || i < 0) break;
                if (YOU.isHost()) {
                    all_entities.get(i).update();
                }
                if (i >= all_entities.size()) break;
                all_entities.get(i).move();
                if (i >= all_entities.size()) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            LinkedList<Orb> all_orbs = new LinkedList<Orb>();
            all_orbs.addAll(ORBS);
            for (int i = 0; i != all_orbs.size(); i++) {
                if (i >= all_orbs.size() || i < 0) break;
                if (GameScreen.YOU.isHost()) all_orbs.get(i).update();
                if (i >= all_orbs.size()) break;
                all_orbs.get(i).move();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        /*Spawn an Orb if there are less than 125 orbs and you are host.*/
        if (ORBS.size() < 125 && YOU.isHost()) {
            spawnOrb(false);
        }
        
        if (leave.isClicked(0)) {
            if (GameScreen.YOU.isHost()) {
                MPServer.stop();
            } else {
                MPClient.disconnect();
            }
            MainMenu.enter();
        }
        
        //track mouse clicks (for projectile shooting and help menu clicking)
        if (SHOW_HELP) {
            if (input.isMousePressed(0)) {
                SHOW_HELP = false;
                GameScreen.notification("Steal all "+getActiveTeams().size()+" Circuits from the enemy Home Bases!", GameScreen.YOU.getTeam().getTeamColor());
            }
            return;
        }
        Entity e = YOU.getEntity();
        double m_x = e.getOnscreenCoords()[0]-Mouse.getX();
        double m_y = Mouse.getY()-e.getOnscreenCoords()[1];
        if (input.isMousePressed(0)) {
            if (YOU.isHost()) {
                if ((int)e.getEnergy() > 10) {
                    e.addEnergy(-5);
                    //spawn a new orb
                    Entity pj = Entity.create("projectile", -1);
                    //set x to outside the circle facing the entity
                    double point[] = e.getRotatedPoint(0, e.getSize() + 20, (int)MiscMath.angleBetween
                (0, 0, -m_x, -m_y));
                    pj.setX(point[0]);
                    pj.setY(point[1]);
                    pj.setTeam(e.getTeam());
                    pj.setVelocityFor(e.getWorldCoords()[0]-m_x, e.getWorldCoords()[1]-m_y, 0, 0, 250, false);
                    GameScreen.addEntity(pj, false, false);
                }
            } else {
                MousePressed m = new MousePressed();
                m.button = 0;
                m.offset_x = m_x;
                m.offset_y = m_y;
                MPClient.sendPacket(m);
            }
        } else if (input.isMousePressed(1)) {
            if (YOU.isHost()) {
                if ((int)e.getEnergy() > 20) {
                    e.addEnergy(-15);
                    //spawn a new orb
                    Entity pj = Entity.create("energyorb", -1);
                    //set x to outside the circle facing the entity
                    double point[] = e.getRotatedPoint(0, e.getSize() + 20, (int)MiscMath.angleBetween
                (0, 0, -m_x, -m_y));
                    pj.setX(point[0]);
                    pj.setY(point[1]);
                    pj.setTeam(e.getTeam());
                    pj.setVelocityFor(e.getWorldCoords()[0]-m_x, e.getWorldCoords()[1]-m_y, -1, -1, 350, false);
                    GameScreen.addEntity(pj, false, false);
                }
            } else {
                MousePressed m = new MousePressed();
                m.button = 1;
                m.offset_x = m_x;
                m.offset_y = m_y;
                MPClient.sendPacket(m);
            }
        }

        //keep track of the state you were in last frame to check if you have just entered the menu
        //onMenuChange, essentially
        SlickInitializer.setLastState(sbg.getCurrentStateID());
        Camera.update();
        MPClient.handleQueue();

    }
    
    public static void renderWorld(Graphics g) {
        g.setColor(Color.white);
        g.fillRect(0, 0, Window.getWidth(), Window.getHeight());
        g.setColor(Color.lightGray);
        g.setLineWidth(2);
        double origin_x = (Window.getWidth()/2)-Camera.WORLD_X;
        double origin_y = (Window.getHeight()/2)-Camera.WORLD_Y;
        for (int x = 0; x != WORLD_WIDTH+1; x++) {
            if (origin_x+(x*64) >= 0 || origin_x+(x*64) <= Window.getHeight()) {
                g.drawLine((float)origin_x+(x*64), (float)origin_y, 
                        (float)origin_x+(x*64), (float)origin_y+(WORLD_HEIGHT*64));
            }
        }
        for (int y = 0; y != WORLD_HEIGHT+1; y++) {
            if (origin_y+(y*64) >= 0 || origin_y+(y*64) <= Window.getHeight()) {
                g.drawLine((float)origin_x, (float)origin_y+(y*64), 
                        (float)(origin_x+(WORLD_WIDTH*64)), (float)origin_y+(y*64));
            }
        }
        g.setLineWidth(1);
        g.setFont(SlickInitializer.NORMAL_FONT);
        
        try {
            LinkedList<Entity> all_entities = new LinkedList<Entity>();
            all_entities.addAll(GAME_OBJECTS);
            all_entities.addAll(ORBS);
            for (int i = 0; i != all_entities.size(); i++) {
                if (i >= all_entities.size()) break;
                Entity e = all_entities.get(i);
                if (!MiscMath.rectangleIntersectsCircle(0, 0, Window.getWidth(), Window.getHeight(), 
                        e.getOnscreenCoords()[0], e.getOnscreenCoords()[1], e.getSize()) && (e instanceof Orb 
                        || e instanceof Player || e instanceof PowerUp)) continue;
                e.draw(g);
            }
            //draw the animations! :D
            for (int i = 0; i != GameScreen.ANIMATIONS.size(); i++) {
                if (i < GameScreen.ANIMATIONS.size()) {
                    Animation t = GameScreen.ANIMATIONS.get(i);
                    t.draw(g);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isCurrentState() {
        return game_state.getCurrentStateID() == SlickInitializer.GAME_SCREEN;
    }
    
    public static Orb getOrb(double world_x, double world_y, int radius, Entity exception) {
        LinkedList<Orb> all_orbs = new LinkedList<Orb>();
        all_orbs.addAll(ORBS);
        for (int i = all_orbs.size()-1; i != -1; i--) {
            Orb e = all_orbs.get(i);
            if (MiscMath.distanceBetween(e.getWorldCoords()[0], e.getWorldCoords()[1], world_x, world_y) 
                    <= Math.abs(radius-e.getSize())) {
                if (!e.equals(exception)) return e;
            }
        }
        return null;
    }
    
    public static Entity getEntity(double world_x, double world_y, int radius, Entity exception) {
        LinkedList<Entity> all_entities = new LinkedList<Entity>();
        all_entities.addAll(GAME_OBJECTS);
        for (int i = all_entities.size()-1; i != -1; i--) {
            Entity e = all_entities.get(i);
            if (MiscMath.distanceBetween(e.getWorldCoords()[0], e.getWorldCoords()[1], world_x, world_y) 
                    <= radius+e.getSize()) {
                if (!e.equals(exception)) return e;
            }
        }
        return null;
    }
    
    public static LinkedList<Entity> getEntities(double world_x, double world_y, double radius, Entity exception) {
        LinkedList<Entity> list = new LinkedList<Entity>();
        LinkedList<Entity> all_entities = new LinkedList<Entity>();
        all_entities.addAll(GAME_OBJECTS);
        for (int i = 0; i != all_entities.size(); i++) {
            Entity e = all_entities.get(i);
            if (MiscMath.distanceBetween(e.getWorldCoords()[0], e.getWorldCoords()[1], world_x, world_y) 
                    <= radius+e.getSize()) {
                if (!e.equals(exception)) list.add(e);
            }
        }
        return list;
    }
    
    public static void addEntity(Entity e, boolean send_packet, boolean force_send) {
        if (e instanceof Orb) { addOrb(((Orb)e)); return; }
        if (!GAME_OBJECTS.contains(e)) {
            if (getEntity(e.getID()) != null) return;
            GAME_OBJECTS.add(e);
            e.onSpawn();
            if (YOU.isHost() && send_packet) {
                AddEntity ae = new AddEntity();
                ae.spawn_code = e.getSpawnCode();
                ae.team_code = e.getTeam().getTeamCode();
                ae.x = e.getWorldCoords()[0];
                ae.y = e.getWorldCoords()[1];
                ae.dx = e.getMovementVector()[0];
                ae.dy = e.getMovementVector()[1];
                ae.ax = e.getMovementVector()[2];
                ae.ay = e.getMovementVector()[3];
                ae.id = e.getID();
                MPServer.sendToAllClients(ae, force_send);
            }
        }
    }
    
    public static void addAnimation(Animation a, boolean send_packet) {
        if (GameScreen.ANIMATIONS.contains(a)) return;
        GameScreen.ANIMATIONS.add(a);
        if (GameScreen.YOU.isHost() && send_packet) {
            AddAnimation aa = new AddAnimation();
            aa.x = a.getWorldCoords()[0];
            aa.y = a.getWorldCoords()[1];
            aa.radius = a.getSize();
            aa.type = a.getType();
            aa.team = a.getTeam().getTeamCode();
            MPServer.sendToAllClients(aa, false);
        }
    }
    
    static void addOrb(Orb e) {
        if (!ORBS.contains(e)) {
            if (getOrb(e.getID()) != null) return;
            ORBS.add(e);
            if (YOU.isHost()) {
                AddEntity ae = new AddEntity();
                ae.spawn_code = e.getSpawnCode();
                ae.team_code = e.getTeam().getTeamCode();
                ae.x = e.getWorldCoords()[0];
                ae.y = e.getWorldCoords()[1];
                ae.dx = e.getMovementVector()[0];
                ae.dy = e.getMovementVector()[1];
                ae.ax = e.getMovementVector()[2];
                ae.ay = e.getMovementVector()[3];
                ae.id = e.getID();
                MPServer.sendToAllClients(ae, false);
            }
        }
    }
    
    public static Entity getEntity(int id) {
        for (int i = 0; i != GAME_OBJECTS.size(); i++) {
            if (GAME_OBJECTS.get(i).getID() == id) {
                return GAME_OBJECTS.get(i);
            }
        }
        return null;
    }
    
    public static Orb getOrb(int id) {
        for (int i = 0; i != ORBS.size(); i++) {
            if (ORBS.get(i).getID() == id) {
                return ORBS.get(i);
            }
        }
        return null;
    }
    
    /**
     * Deletes the specified entity e.
     * @param e The entity to delete.
     * @param silent If true, onDelete will not be called, a packet will not be sent, an animation will not be created.
     */
    public static void deleteEntity(Entity e, boolean silent) {
        if (e instanceof Orb) { deleteOrb(((Orb)e), silent); return; }
        if (GAME_OBJECTS.contains(e)) {
            GAME_OBJECTS.remove(e);
            if (silent) return;
            RemoveEntity rm = new RemoveEntity();
            rm.orb = false;
            rm.id = e.getID();
            if (YOU.isHost()) MPServer.sendToAllClients(rm, false);
            e.getTeam().removeEntity(e);
            e.onDelete();
            Animation.create(e.getWorldCoords()[0], e.getWorldCoords()[1], e.getSize()+20, Animation.RADIATE_INWARD, 
                        e.getTeam(), false);
        }
    }
    
    static void deleteOrb(Orb e, boolean silent) {
        if (ORBS.contains(e)) {
            ORBS.remove(e);
            if (silent) return;
            RemoveEntity rm = new RemoveEntity();
            rm.orb = true;
            rm.id = e.getID();
            if (YOU.isHost()) MPServer.sendToAllClients(rm, false);
        }
    }
    
    public static void addPlayerCard(PlayerCard p) {
        if (!PLAYERS.contains(p)) {
            PLAYERS.add(p);
        }
    }
    
    public static void deletePlayerCard(PlayerCard e) {
        if (PLAYERS.contains(e)) {
            if (GameScreen.YOU.isHost()) {
                RemovePlayer rm = new RemovePlayer();
                rm.index = PLAYERS.indexOf(e);
                MPServer.sendToAllClients(rm, false);
            }
            deleteEntity(e.getEntity(), false);
            PLAYERS.remove(e);
            e.getTeam().removeMember(e);
            if (YOU.equals(e)) {
                YOU = null;
            }
        }
    }
    
    public static void deletePlayerCard(int index) {
        if (index > -1 && index < PLAYERS.size()) {
            PlayerCard e = PLAYERS.get(index);
            deletePlayerCard(e);
        }
    }


    /**
     * The user input being entered.
     * @return An Input instance to use for input checking.
     */
    public static Input getInput() {
        return input;
    }

    /**
     * Deletes the player list and resets all teams back to default (no points, no home, no members).
     */
    public static void deletePlayerCards() {
        PLAYERS.clear();
        for (int i = 0; i != AVAILABLE_TEAMS.length; i++) {
            AVAILABLE_TEAMS[i].reset();
        }
    }
        
    /**
     * Clears all game objects and orbs, resets the team scores to 0,
     * and resets player stats to default (powerups and key presses).
     */
    public static void resetGame() {
        GAME_OBJECTS.clear();
        ORBS.clear();
        ANIMATIONS.clear();
        for (int i = 0; i != AVAILABLE_TEAMS.length; i++) {
            AVAILABLE_TEAMS[i].setPoints(0);
            AVAILABLE_TEAMS[i].setOrbCount(0);
        }
        for (int i = 0; i != PLAYERS.size(); i++) {
            PLAYERS.get(i).reset();
        }
        if (GameScreen.YOU.isHost()) MPServer.sendToAllClients(new ResetGame(), false);
    }
    
    public static PlayerCard getPlayerCard(Connection c) {
        for (PlayerCard p: PLAYERS) {
            if (c.equals(p.getConnection())) {
                return p;
            }
        }
        return null;
    }

    public void keyPressed(int key, char c) {
        //if key is number key, select hotbar
        int hotkey_clicked = key-2;
        if (hotkey_clicked >= 0 && hotkey_clicked <= 9) {
            if (GameScreen.YOU.isHost()) {
                int id = YOU.getPowerup(hotkey_clicked);
                YOU.spawnPowerup(hotkey_clicked);
            }
        }

        GameScreen.YOU.addPressedKey(key);
        int dx = 0, dy = 0;
        double m = 1;
        if (GameScreen.YOU.isKeyPressed(Input.KEY_LSHIFT)) m = 2;
        if (GameScreen.YOU.isKeyPressed(Input.KEY_W)) dy -= 100*m;
        if (GameScreen.YOU.isKeyPressed(Input.KEY_A)) dx -= 100*m;
        if (GameScreen.YOU.isKeyPressed(Input.KEY_S)) dy += 100*m;
        if (GameScreen.YOU.isKeyPressed(Input.KEY_D)) dx += 100*m;
        if ((dx < 0 && dy < 0) || (dx > 0 && dy < 0) 
                || (dx > 0 && dy > 0) || (dx < 0 && dy > 0)) {
            dx*=0.7;dy*=0.7;
        }
        
        if (GameScreen.YOU.isHost()) YOU.getEntity().setVelocity(dx, dy, 0, 0, true);
        
        if (key == Input.KEY_E && GameScreen.YOU.isHost()) {
            Player p = GameScreen.YOU.getEntity();
            Entity projectile = Entity.create("projectile", -1);
            double endpoint[] = p.getRotatedPoint(0, p.getSize()+30, 
                            (int)MiscMath.angleBetween(p.getOnscreenCoords()[0], p.getOnscreenCoords()[1], 
                                    p.getOnscreenCoords()[0]+(dx*100), p.getOnscreenCoords()[1]+(100*dy)));
            projectile.setX(p.getWorldCoords()[0]+endpoint[0]);
            projectile.setY(p.getWorldCoords()[1]+endpoint[1]);
            projectile.setVelocityFor(p.getWorldCoords()[0]+endpoint[0]+(dx*100), 
                    p.getWorldCoords()[1]+endpoint[1]+(dy*100), -1, -1, 400, false);
            GameScreen.addEntity(projectile, false, false);
        }
        
        if (GameScreen.YOU.isHost() == false) {
            KeyPressed k = new KeyPressed();
            k.key = key;
            MPClient.sendPacket(k);
        }
        
    }
    
    public static ArrayList<TeamCard> getActiveTeams() {
        ArrayList<TeamCard> temz = new ArrayList<TeamCard>();
        for (TeamCard t: AVAILABLE_TEAMS) {
            if (t.memberCount() > 0) temz.add(t);
        }
        return temz;
    }
    
    public void keyReleased(int key, char c) {
        //if key is number key, select hotbar
        /*int hotkey_clicked = key-2;
        if (hotkey_clicked >= 0 && hotkey_clicked <= 9) {
            
        }*/
        GameScreen.YOU.removePressedKey(key);
        int dx = 0, dy = 0;
        double m = 1;
        if (GameScreen.YOU.isKeyPressed(Input.KEY_LSHIFT)) m = 2;
        if (GameScreen.YOU.isKeyPressed(Input.KEY_W)) dy -= 100*m;
        if (GameScreen.YOU.isKeyPressed(Input.KEY_A)) dx -= 100*m;
        if (GameScreen.YOU.isKeyPressed(Input.KEY_S)) dy += 100*m;
        if (GameScreen.YOU.isKeyPressed(Input.KEY_D)) dx += 100*m;
        if ((dx < 0 && dy < 0) || (dx > 0 && dy < 0) 
                || (dx > 0 && dy > 0) || (dx < 0 && dy > 0)) {
            dx*=0.7;dy*=0.7;
        }
        
        if (GameScreen.YOU.isHost()) YOU.getEntity().setVelocity(dx, dy, 0, 0, true);
        
        if (GameScreen.YOU.isHost() == false) {
            KeyReleased k = new KeyReleased();
            k.key = key;
            MPClient.sendPacket(k);
        }
        
    }
    
    public static void spawnOrb(boolean send_packet) {
        Orb hb;
        if (Math.abs(new Random().nextInt() % 10) == 0) {
            hb = ((Orb)Entity.create("energyorb", -1));
        } else {
            hb = ((Orb)Entity.create("orb", -1));
        }
        hb.setX(64*(8+(Math.abs(new Random().nextInt() % (WORLD_WIDTH-16)))));
        hb.setY(64*(8+(Math.abs(new Random().nextInt() % (WORLD_WIDTH-16)))));
        addEntity(hb, false, false);
    }
    
    public static void spawnPowerup(boolean send_packet) {
        PowerUp hb;
        int type = 1+Math.abs(new Random().nextInt() % 6);
        hb = ((PowerUp)Entity.create("powerup_"+type, -1));
        hb.setX(64*(8+(Math.abs(new Random().nextInt() % (WORLD_WIDTH-16)))));
        hb.setY(64*(8+(Math.abs(new Random().nextInt() % (WORLD_WIDTH-16)))));
        addEntity(hb, false, false);
    }
    
    public static void generateWorld() {
        
        for (int i = 0; i != 125; i++) {
            spawnOrb(false);
        }
        
        for (int i = 0; i != 12; i++) {
            spawnPowerup(false);
        } 
        
        //spawn the neutral turret
        Entity n_turret;
        n_turret = Entity.create("turret", -1);
        n_turret.setX(64*(WORLD_WIDTH/2));
        n_turret.setY(64*(WORLD_HEIGHT/2));
        addEntity(n_turret, false, false);
        
        int c = 0, hx = 0, hy = 0;
        for (int i = 0; i != AVAILABLE_TEAMS.length; i++) {
            TeamCard t = AVAILABLE_TEAMS[i];
            System.out.println(t.getName()+" members: "+t.memberCount());
            if (t.getTeamCode() != TeamCard.NEUTRAL_TEAM && t.memberCount() > 0) {
                HomeBase hb = ((HomeBase)Entity.create("homebase", -1));
                hb.setTeam(t);
                t.setHomeBase(hb);
                if (c == 0) {hx = 64*3; hy = 64*3;}
                if (c == 1) {hx = 64*(WORLD_WIDTH - 3); hy = 64*(WORLD_HEIGHT - 3);}
                if (c == 2) {hx = 64*(WORLD_WIDTH - 3); hy = 64*3;}
                if (c == 3) {hx = 64*3; hy = 64*(WORLD_HEIGHT - 3);}
                hb.setX(hx);
                hb.setY(hy);
                addEntity(hb, false, false);
                c++;
            }
        }
        
        for (PlayerCard p: PLAYERS) {
            Player e = ((Player)Entity.create("player", -1));
            p.setEntity(e);
            e.setTeam(p.getTeam());
            e.setX(e.getTeam().getHomeBase().getWorldCoords()[0]);
            e.setY(e.getTeam().getHomeBase().getWorldCoords()[1]);
            addEntity(e, true, true);
            UpdatePlayerCard u = new UpdatePlayerCard();
            u.card_index = PLAYERS.indexOf(p);
            u.team_code = p.getTeam().getTeamCode();
            u.entity_id = e.getID();
            MPServer.sendToAllClients(u, true);
        }
        
        StartGame s = new StartGame();
        Camera.WORLD_X = (64*WORLD_WIDTH)/2;
        Camera.WORLD_Y = (64*WORLD_HEIGHT)/2;
        MPServer.sendToAllClients(s, true);
        
    }
    
    public static void enter() {
        game_state.enterState(SlickInitializer.GAME_SCREEN);
        SHOW_HELP = true;
    }

}
