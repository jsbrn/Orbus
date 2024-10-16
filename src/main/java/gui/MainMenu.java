package gui;


import gui.elements.Button;
import gui.elements.ListDisplay;
import gui.elements.TextDisplay;
import gui.elements.Textbox;
import main.SlickInitializer;
import misc.SoundManager;
import misc.Window;
import network.client.MPClient;
import network.server.MPServer;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.*;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainMenu extends BasicGameState {
    
    MPServer SERVER;
    MPClient CLIENT;
    
    Image title, lanheader;
    
    static StateBasedGame state;
    
    Button host, refresh, get_update;
    public static Button CONNECT_BUTTON;
    Textbox ip;
    ListDisplay lan_games;
    ArrayList<String> lan_ips;

    public MainMenu(int state) {
    }

    @Override
    public int getID() {
        return -1;
    }
    
    public void loadAssets() throws SlickException {
        System.out.println("Loading images...");
        lan_ips = new ArrayList<String>();
        lan_games = new ListDisplay(300);
        lan_games.setBackgroundColor(new Color(0, 0, 0, 140));
        lan_games.setTextColor(Color.white);
        lan_games.addLine("-");
        title = new Image("images/title.png", false, Image.FILTER_NEAREST);
        System.out.println("Loading sounds...");
        SoundManager.loadSounds();
        
        SlickInitializer.NORMAL_FONT = new UnicodeFont("fonts/amble.ttf", 16, false, false);
        SlickInitializer.NORMAL_FONT.addAsciiGlyphs();
        SlickInitializer.NORMAL_FONT.addGlyphs(0, 255);
        SlickInitializer.NORMAL_FONT.getEffects().add(new ColorEffect());
        SlickInitializer.NORMAL_FONT.loadGlyphs();
        SlickInitializer.SMALL_FONT = new UnicodeFont("fonts/amble.ttf", 8, false, false);
        SlickInitializer.SMALL_FONT.addAsciiGlyphs();
        SlickInitializer.SMALL_FONT.addGlyphs(0, 255);
        SlickInitializer.SMALL_FONT.getEffects().add(new ColorEffect());
        SlickInitializer.SMALL_FONT.loadGlyphs();

    }

    //loads images and such
    @Override
    public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
        get_update = new Button("Update available: click to download");
        loadAssets();
        host = new Button("Start a game");
        refresh = new Button("Scan for games");
        CONNECT_BUTTON = new Button("Connect");
        ip = new Textbox(300, 1);
        ip.setEmptyText("Enter an IP and click Connect", Color.gray);
        lanheader = new Image("images/lanheader.png");
    }

    @Override
    public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {

        //Window.scanForResizes(gc);

        //g.scale((int)Screen.getWindowWidth() / 800, (int)Screen.getWindowHeight() / (int)Screen.getWindowHeight());
        gc.getInput().enableKeyRepeat();
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
        ip.setX(Window.getWidth()/2 - ip.getWidth()/2 - (CONNECT_BUTTON.getWidth()+10)/2);
        ip.setY(Window.getHeight()/2 - ip.getHeight()/2);
        host.setX(Window.getWidth()/2 - host.getWidth()/2);
        host.setY(Window.getHeight()/2 - 60);
        CONNECT_BUTTON.setX(ip.getX() + ip.getWidth() + 12);
        CONNECT_BUTTON.setY(Window.getHeight()/2 - CONNECT_BUTTON.getHeight()/2);
        ip.draw(g);
        host.draw(g);
        CONNECT_BUTTON.draw(g);
        
        refresh.setX(Window.getWidth()/2 - refresh.getWidth()/2);
        refresh.setY(lan_games.getY()+lan_games.getHeight()+10);
        refresh.draw(g);
        lan_games.setX(Window.getWidth()/2 - lan_games.getWidth()/2);
        lan_games.setY(ip.getY() + 100);
        lan_games.draw(g);
        g.drawImage(lanheader, (int)lan_games.getX()-1, (int)lan_games.getY()-lanheader.getHeight());
        
        g.drawImage(title, Window.getWidth()/2-title.getWidth()/2, (float)host.getY() - 40 - title.getHeight());
        g.setColor(Color.black);
        g.setFont(SlickInitializer.NORMAL_FONT);
        g.drawString("(uses port 6575)", (float)ip.getX(), (float)ip.getY() + ip.getHeight() + 10);
        
        get_update.show();
        if (SlickInitializer.UPDATE_AVAILABLE == false) get_update.hide();
        get_update.setX(10);
        get_update.setY(Window.getHeight() - 32 - 10);
        get_update.draw(g);
        if (get_update.isClicked(0)) {
            try {
                Desktop.getDesktop().browse(new URL("http://computerology.itch.io/orbgame").toURI());
            } catch (URISyntaxException ex) {
                Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        for (int i = 0; i != GameScreen.TEXT_POPUPS.size(); i++) {
            if (i < GameScreen.TEXT_POPUPS.size()) {
                TextDisplay t = GameScreen.TEXT_POPUPS.get(i);
                t.update();
                t.draw(g);
            }
        }
        
    }

    @Override
    public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
        Input i = gc.getInput();
        state = sbg;
        ip.update();
        
        if (refresh.isClicked(0)) {
            lan_games.clearText();
            lan_games.addLine("- searching -");
            refresh.disable();
            lan_ips = MPClient.getLANGames();
            refresh.enable();
            lan_games.clearText();
            for (String s: lan_ips) {
                lan_games.addLine(s);
            }
            if (lan_ips.isEmpty()) {
                lan_games.addLine("- none found -");
            }
        }
        
        GameScreen.DELTA_TIME = delta;
        GameScreen.input = gc.getInput();
        if (host.isClicked(0)) { 
            GameScreen.deletePlayerCards();
            LobbyMenu.enter(false);
            SERVER = new MPServer(6575);
        }
        if (CONNECT_BUTTON.isClicked(0) && ip.getText().length() > 0) {
            GameScreen.deletePlayerCards();
            CONNECT_BUTTON.disable();
            LobbyMenu.enter(false);
            CLIENT = new MPClient();
            CLIENT.connect(ip.getText(), 6575);
        }
        SlickInitializer.setLastState(sbg.getCurrentStateID());

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
                ip.addText(text);
            }
        } else {
            ip.backspace();
        }
        
    }
    
    public static void enter() {
        state.enterState(SlickInitializer.MAIN_MENU);
    }

}
