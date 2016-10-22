package com.bitbucket.computerology.gui;

import com.bitbucket.computerology.gui.elements.Button;
import com.bitbucket.computerology.gui.elements.ListDisplay;
import com.bitbucket.computerology.gui.elements.TextDisplay;
import com.bitbucket.computerology.gui.elements.Textbox;
import com.bitbucket.computerology.main.SlickInitializer;
import com.bitbucket.computerology.misc.MiscMath;
import com.bitbucket.computerology.misc.Window;
import com.bitbucket.computerology.network.Packet;
import com.bitbucket.computerology.network.Packet.AddPlayer;
import com.bitbucket.computerology.network.Packet.PlayerNameChange;
import com.bitbucket.computerology.network.Packet.RemovePlayer;
import com.bitbucket.computerology.network.Packet.TeamToggle;
import com.bitbucket.computerology.network.client.MPClient;
import com.bitbucket.computerology.network.server.MPServer;
import com.bitbucket.computerology.world.PlayerCard;
import com.bitbucket.computerology.world.TeamCard;
import java.util.LinkedList;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Color;


import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public class LobbyMenu extends BasicGameState {
    
    ListDisplay player_list;
    Button change_team, start_game, leave, change_name;
    Button[] add_bot_buttons;
    Textbox name_field;
    static StateBasedGame state;
    static boolean show_game_screen = false;
    Image kick, lobbyheader;

    public LobbyMenu(int state) {

    }

    @Override
    public int getID() {
        return -5;
    }

    @Override
    public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
        player_list = new ListDisplay(400);
        change_team = new Button("Toggle Team");
        start_game = new Button("Start Game");
        leave = new Button("Disconnect");
        change_name = new Button("Change Name");
        name_field = new Textbox(170, 1);
        name_field.setEmptyText("Type your name here", Color.gray);
        state = sbg;
        kick = new Image("resources/images/kick.png", false, Image.FILTER_LINEAR);
        add_bot_buttons = new Button[]{
        new Button("Add Red Bot"),
        new Button("Add Blue Bot"),
        new Button("Add Green Bot"),
        new Button("Add Purple Bot")};
        lobbyheader = new Image("resources/images/lobbyheader.png", false, Image.FILTER_NEAREST);
    }

    public LobbyMenu() {
    }

    //draws state (screen) elements
    @Override
    public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
        gc.getInput().enableKeyRepeat();
        //g.scale((int)Screen.getWindowWidth() / 800, (int)Screen.getWindowHeight() / (int)Screen.getWindowHeight());
        if (!show_game_screen) {
            double origin_x = -1, origin_y = -1;
            g.setColor(Color.white);
            g.fillRect(0, 0, Window.getWidth(), Window.getHeight());
            g.setColor(Color.lightGray);
            g.setLineWidth(2);
            for (int x = 0; x != Window.getWidth(); x++) {
                g.drawLine((float)origin_x+(x*64), (float)origin_y, 
                        (float)origin_x+(x*64), (float)origin_y+(Window.getWidth()*64));
            }
            for (int y = 0; y != Window.getHeight(); y++) {
                g.drawLine((float)origin_x, (float)origin_y+(y*64), 
                        (float)(origin_x+(Window.getWidth()*64)), (float)origin_y+(y*64));
            }
            g.setLineWidth(1);
        } else {
            GameScreen.renderWorld(g);
        }
        g.setColor(Color.black);
        if (GameScreen.YOU == null) return;
        player_list.clearText();
        player_list.setTitleColor(Color.yellow);
        player_list.setX(Window.getWidth()/2 - player_list.getWidth()/2);
        player_list.setY(Window.getHeight()/2 - player_list.getHeight()/2 - 36);
        player_list.setBackgroundColor(new Color(0, 0, 0, 140));
        player_list.setTextColor(Color.white);
        int lc = 0;
        for (int i = 0; i != GameScreen.AVAILABLE_TEAMS.length; i++) {
            TeamCard t = GameScreen.AVAILABLE_TEAMS[i];
            if (t.memberCount() > 0) {
                player_list.addLine(t.getName());
                lc++;
                for (int ii = 0; ii != t.memberCount(); ii++) {
                    if (ii < t.memberCount()) {
                        String name = t.getMembers().get(ii).getName();
                        if (t.getMembers().get(ii).equals(GameScreen.YOU)) {
                            name += " (You)";
                        }
                        if (t.getMembers().get(ii).isBot()) {
                            name += " (BOT)";
                        }
                        player_list.addLine("     "+name);
                        lc++;
                    } else {
                        player_list.addLine("     -");
                        lc++;
                    }
                }
            }
        }
        for (int i = 0; i != 20-lc; i++) {
            player_list.addLine("");
        }
        player_list.draw(g);
        for (int i = 0; i != 20; i++) {
            if (player_list.getLine(i).length() == 0) continue;
            if ((player_list.getLine(i).charAt(0) == ' ') && GameScreen.YOU.isHost()) {
                g.drawImage(kick, (float)(player_list.getX() + player_list.getWidth() - 25), 
                        (float)(player_list.getY() + 1 + (i*20)));
            }
        }
        
        g.drawImage(lobbyheader, (int)player_list.getX()-1, (int)player_list.getY()-lobbyheader.getHeight());
        
        start_game.setX(player_list.getX());
        start_game.setY(player_list.getY() + player_list.getHeight() + 10);
        leave.setX(player_list.getX() + player_list.getWidth() - leave.getWidth());
        leave.setY(player_list.getY() + player_list.getHeight() + 10);
        name_field.setX(player_list.getX());
        name_field.setY(leave.getY() + leave.getHeight() + 15);
        change_name.setX(player_list.getX() + name_field.getWidth() + 10);
        change_name.setY(leave.getY() + leave.getHeight() + 12);
        change_team.setX(Window.getWidth()/2 - change_team.getWidth()/2);
        if (!GameScreen.YOU.isHost()) change_team.setX(player_list.getX());
        change_team.setY(player_list.getY() + player_list.getHeight() + 10);
        start_game.draw(g);
        change_team.draw(g);
        change_name.draw(g);
        name_field.draw(g);
        leave.draw(g);
        
        for (int i = 0; i != add_bot_buttons.length; i++) {
            Button b = add_bot_buttons[i];
            b.setX(player_list.getX()+player_list.getWidth()+10);
            b.setY(player_list.getY()+(i*40));
            b.draw(g);
        }
        
        for (int i = 0; i != GameScreen.TEXT_POPUPS.size(); i++) {
            if (i < GameScreen.TEXT_POPUPS.size()) {
                TextDisplay t = GameScreen.TEXT_POPUPS.get(i);
                t.update();
                t.draw(g);
            }
        }

    }
    
    /**
     * Enter the lobby menu.
     * @param show Whether you wish to show the lobby menu as an overlay 
     * on top of the game world.
     */
    public static void enter(boolean show) {
        show_game_screen = show;
        state.enterState(SlickInitializer.LOBBY_MENU);
    }

    //key binding and calling update() in all objects
    @Override
    public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
        GameScreen.DELTA_TIME = delta;
        MPClient.handleQueue();
        SlickInitializer.setLastState(sbg.getCurrentStateID());
        if (GameScreen.YOU == null) return;
        
        if (Window.wasResized()) {
            if (!GameScreen.YOU.isHost()) {
                Packet.WindowDimensions w = new Packet.WindowDimensions();
                w.w = Window.getWidth();
                w.h = Window.getHeight();
                w.player_index = GameScreen.PLAYERS.indexOf(GameScreen.YOU);
                MPClient.sendPacket(w);
                GameScreen.YOU.setWindowDimensions(Window.getWidth(), Window.getHeight());
            } else {
                GameScreen.YOU.setWindowDimensions(Window.getWidth(), Window.getHeight());
            }
        }
        
        if (GameScreen.YOU.isHost()) { 
            start_game.enable(); 
        } else { 
            start_game.disable();
        }
        if (GameScreen.YOU.isHost()) start_game.show(); else start_game.hide();
        if (start_game.isClicked(0) && GameScreen.YOU.isHost()) {
            if (GameScreen.getActiveTeams().size() > 1) {
                GameScreen.resetGame();
                GameScreen.generateWorld();
                GameScreen.enter();
            } else {
                GameScreen.notification("You need at least one more team to start a game!", Color.red);
            }
        }
        
        if (change_name.isClicked(0) && name_field.getText().trim().length() > 0) {
            PlayerNameChange n = new PlayerNameChange();
            n.new_name = name_field.getText().trim();
            n.player_index = GameScreen.PLAYERS.indexOf(GameScreen.YOU);
            if (GameScreen.YOU.isHost()) {
                MPServer.sendToAllClients(n, false);
                name_field.clear();
                GameScreen.YOU.setName(n.new_name);
            } else {
                MPClient.sendPacket(n);
                name_field.clear();
            }
        }
        
        if (leave.isClicked(0)) {
            sbg.enterState(SlickInitializer.MAIN_MENU);
            if (GameScreen.YOU.isHost()) {
                MPServer.stop();
            } else {
                MPClient.disconnect();
            }
        }
        
        if (change_team.isClicked(0)) {
            if (GameScreen.YOU.isHost()) {
                GameScreen.YOU.toggleTeam();
                TeamToggle tt = new TeamToggle();
                tt.index = GameScreen.PLAYERS.indexOf(GameScreen.YOU);
                MPServer.sendToAllClients(tt, false);
            } else {
                TeamToggle tt = new TeamToggle();
                tt.index = GameScreen.PLAYERS.indexOf(GameScreen.YOU);
                MPClient.sendPacket(tt);
            }
        }
        
        for (int i = 0; i != add_bot_buttons.length; i++) {
            Button b = add_bot_buttons[i];
            if (GameScreen.YOU.isHost() == false) {
                b.hide();
            }
            if (GameScreen.PLAYERS.size() >= 16) {
                b.disable();
                continue;
            } else {
                b.enable();
            }
            if (b.isClicked(0)) {
                //add a bot
                PlayerCard p = new PlayerCard();
                p.setTeam(GameScreen.AVAILABLE_TEAMS[i]);
                GameScreen.addPlayerCard(p);
                AddPlayer ap = new AddPlayer();
                ap.name = p.getName();
                ap.team_code = i;
                ap.you = false;
                ap.host = false;
                ap.bot = true;
                MPServer.sendToAllClients(ap, false);
            }
        }
        
        //check for mouse clicks
        if (gc.getInput().isMousePressed(0) && GameScreen.YOU.isHost()) {
            //get list of nonempty teams
            LinkedList<TeamCard> nonempty_teams = new LinkedList<TeamCard>();
            for (TeamCard t: GameScreen.AVAILABLE_TEAMS) {
                if (!t.getMembers().isEmpty()) {
                    nonempty_teams.add(t);
                }
            }
            int team = -1, player_index = -1;
            for (int i = 0; i != 20; i++) {
                if (player_list.getLine(i).length() == 0) continue;
                if ((player_list.getLine(i).charAt(0) == ' ')) {
                    player_index++;
                } else {
                    team++;
                    player_index = -1;
                }
                if (MiscMath.pointIntersects(Mouse.getX(), Window.getHeight()-Mouse.getY(), 
                        (float)(player_list.getX() + player_list.getWidth() - 25), 
                            (float)(player_list.getY() + 1 + (i*20)), 20, 20)) {
                    if (team > -1 && team < nonempty_teams.size()) {
                        if (player_index > -1 && player_index < nonempty_teams.get(team).getMembers().size()) {
                            PlayerCard p = nonempty_teams.get(team).getMembers().get(player_index);
                            if (p.isHost() == false) {
                                if (p.getConnection() != null) {
                                    p.getConnection().close();
                                } else {
                                    GameScreen.deletePlayerCard(p);
                                }
                                RemovePlayer rm = new RemovePlayer();
                                rm.index = GameScreen.PLAYERS.indexOf(p);
                                MPServer.sendToAllClients(rm, false);
                                break;
                            }
                        }
                    }
                }
            }
        }
        
    }
    
    public void keyPressed(int key, char c) {
        int[] illegal = {Input.KEY_LSHIFT, Input.KEY_LCONTROL,
        Input.KEY_RSHIFT, Input.KEY_RCONTROL, Input.KEY_SLASH, 
        Input.KEY_BACKSLASH, Input.KEY_ESCAPE, Input.KEY_ENTER,
        Input.KEY_HOME, Input.KEY_END, Input.KEY_INSERT, Input.KEY_DELETE, 
        Input.KEY_DOWN, Input.KEY_LEFT, Input.KEY_RIGHT, Input.KEY_UP, 
        Input.KEY_CAPITAL, Input.KEY_LALT, Input.KEY_RALT, Input.KEY_UNLABELED, 
        Input.KEY_PAUSE, Input.KEY_APPS, Input.KEY_NUMLOCK,
        Input.KEY_SCROLL, Input.KEY_F1,Input.KEY_F2,Input.KEY_F3,Input.KEY_F4,Input.KEY_F5,
        Input.KEY_F6,Input.KEY_F7,Input.KEY_F8,Input.KEY_F8,Input.KEY_F9,
        Input.KEY_F10,Input.KEY_F11,Input.KEY_F12, Input.KEY_INSERT, Input.KEY_TAB,
        Input.KEY_LWIN, Input.KEY_RWIN};
        
        if (Input.KEY_BACK != key) {
            String text = c+"";
            for (int i = 0; i != illegal.length; i++) {
                if (illegal[i] == key) {
                    text = "";
                }
            }
            if (GameScreen.getInput().isKeyDown(Input.KEY_LSHIFT)) {
                text = text.toUpperCase();
            }
            if (!GameScreen.getInput().isKeyDown(Input.KEY_LCONTROL)
                    && !GameScreen.getInput().isKeyDown(Input.KEY_RCONTROL)) {
                name_field.addText(text);
            }
        } else {
            name_field.backspace();
        }
        
    }

}

