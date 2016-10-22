package com.bitbucket.computerology.main;

import com.bitbucket.computerology.gui.*;
import com.bitbucket.computerology.misc.Window;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.state.StateBasedGame;

public class SlickInitializer extends StateBasedGame {

    //the name of the window
    public static final String WINDOW_TITLE = "Orbus";
    //set ids to the states
    public static final int MAIN_MENU = -1,
            GAME_SCREEN = 0,
            INVENTORY_MENU=  -2,
            CATALOG_MENU = -3,
            MANAGE_MENU = -8,
            PAUSE_MENU = -4,
            LOBBY_MENU = -5,
            ACHIEVEMENTS_MENU = -6, SHEEP_MENU = -9;
    static int LAST_STATE = 0;

    public static boolean UPDATE_AVAILABLE = false;
    public static int UPDATE_ID = 15;
    public static String VERSION_NAME = "beta 1.1";

    boolean initialized = false;

    public static UnicodeFont SUBTITLE_FONT, TITLE_FONT;
    public static UnicodeFont NORMAL_FONT, SMALL_FONT;

    //create a window object
    public static AppGameContainer WINDOW_INSTANCE;

    public SlickInitializer(String gameTitle) {

        super(gameTitle); //set window title to "gameTitle" string

        //add states
        addState(new MainMenu(MAIN_MENU));
        addState(new GameScreen(GAME_SCREEN));
        addState(new LobbyMenu(LOBBY_MENU));
    }

    @Override
    public void initStatesList(GameContainer gc) throws SlickException {
        if (initialized == false) {
            //initialize states
            getState(MAIN_MENU).init(gc, this);
            getState(GAME_SCREEN).init(gc, this);
            getState(LOBBY_MENU).init(gc, this);

            //load "menu" state on startup
            this.enterState(MAIN_MENU);
            initialized = true;
        }
    }

    public static void main(String args[]) throws IOException {
        createRootDirectory(); //make a home for the save files
        setLocalVersion(); //update the client data so the launcher can check for updates
        UPDATE_AVAILABLE = checkForUpdates();
        //the textures and entity lists are loaded in GameScreen.init() now
        try {
            //set window properties
            WINDOW_INSTANCE = new AppGameContainer(new SlickInitializer(WINDOW_TITLE+" ("+VERSION_NAME+")"));
            WINDOW_INSTANCE.setDisplayMode(820, Window.MIN_HEIGHT, false);
            WINDOW_INSTANCE.setFullscreen(false);
            WINDOW_INSTANCE.setShowFPS(false);
            WINDOW_INSTANCE.setVSync(true);
            WINDOW_INSTANCE.setAlwaysRender(true);
            WINDOW_INSTANCE.setResizable(true);
            WINDOW_INSTANCE.setIcons(new String[]{"resources/images/orbus_logo_16.png"
            ,"resources/images/orbus_logo_32.png",
            "resources/images/orbus_logo_64.png",
            "resources/images/orbus_logo.png"});
            WINDOW_INSTANCE.start();
        } catch (SlickException e) {
            e.printStackTrace();
        }
    }

    public static void createRootDirectory() {
        File root = new File(System.getProperty("user.home")+"/sheepfarmsimulator/");
        File save_folder = new File(System.getProperty("user.home")+"/sheepfarmsimulator/saves");
        if (root.exists() == false) {
            root.mkdir();
        }
        if (save_folder.exists() == false) {
            save_folder.mkdir();
        }
    }

    public static void setLocalVersion() {
        //register the local version so the launcher can compare to the most recent update
        Properties prop = new Properties();
        try {
            prop.setProperty("updateID", ""+UPDATE_ID); //make sure this matches the server's updateID
            prop.setProperty("name", ""+VERSION_NAME);
            prop.store(
                    new FileOutputStream(System.getProperty("user.home") + "/sheepfarmsimulator/client.properties"), null);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void setLastState(int ls) {
        LAST_STATE = ls;
    }

    public static int getLastState() {
        return LAST_STATE;
    }
    
    public static boolean download(String source, String dest) {
        System.out.println("Downloading update information...");
        
        URL url; //represents the location of the file we want to dl.
        URLConnection con;  // represents a connection to the url we want to dl.
        DataInputStream dis;  // input stream that will read data from the file.
        FileOutputStream fos; //used to write data from input stream to file.

        byte[] fileData;  //byte aray used to hold data from downloaded file

        //download the file
        int last_progress = -1;
        try {
            url = new URL(source);
            con = url.openConnection();
            dis = new DataInputStream(con.getInputStream());
            fileData = new byte[con.getContentLength()];

            int progress = 0;

            for (float x = 0; x < con.getContentLength(); x++) {

                fileData[(int) x] = dis.readByte();
                if (x > 0) {
                    progress = (int) (x / con.getContentLength() * 100);
                    last_progress = progress;
                }

            }

            dis.close(); // close the data input stream
            fos = new FileOutputStream(dest);
            fos.write(fileData);
            fos.close();

            System.out.println("Download complete!");
            return true;

        } catch (MalformedURLException m) {
            System.out.println(m);
            m.printStackTrace();
        } catch (IOException io) {
            System.out.println(io);
            io.printStackTrace();
        }
        return false;
    }
    
    public static boolean checkForUpdates() {
        new File(System.getProperty("user.home")+"/orbgame/").mkdir();
        if (!download("http://computerology.bitbucket.org/orbgame/update.properties", System.getProperty("user.home")+"/orbgame/update.properties"));
        Properties prop = new Properties();
        try {
            FileInputStream f = new FileInputStream(System.getProperty("user.home")+"/orbgame/update.properties");
            prop.load(f);
            int id = Integer.parseInt(prop.getProperty("updateID"));
            f.close();
            if (id > UPDATE_ID) return true; 
        } catch (IOException ex) {
            Logger.getLogger(SlickInitializer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
}
