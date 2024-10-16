package world;

import com.esotericsoftware.kryonet.Connection;
import gui.GameScreen;
import misc.MiscMath;
import network.Packet.UpdatePowerupList;
import network.server.MPServer;
import org.newdawn.slick.Color;
import world.entities.Entity;
import world.entities.homebase.HomeBase;
import world.entities.orbs.Orb;
import world.entities.part.CollectorPart;
import world.entities.player.Player;
import world.entities.powerups.PowerUp;
import world.entities.projectiles.EnergyBombProjectile;

import java.util.LinkedList;
import java.util.Random;

import static gui.GameScreen.addEntity;

/**
 * The class representing the player with all it's stats and whatnot.
 */
public class PlayerCard {
    
    TeamCard team;
    int[] window_dims;
    
    Connection conn; //if null, you are a bot, or the host
    boolean host, bot;
    Player entity; //the entity you control (if null, you are spectating!)
    String name;
    
    public static int POWERUP_LIMIT = 4;
    
    LinkedList<Entity> visible_entities;
    LinkedList<Integer> pressed_keys;
    
    int[] powerups;
    
    AI bot_ai;
    
    public PlayerCard(String name) {
        this.name = name;
        this.bot = false;
        this.bot_ai = null;
        this.host = false;
        this.powerups = new int[]{-1, -1, -1, -1};
        this.pressed_keys = new LinkedList<Integer>();
        this.window_dims = new int[]{10000, 10000};
        this.visible_entities = new LinkedList<Entity>();
    }
    
    /**
     * Creates a bot, with a random name and a random behaviour.
     */
    public PlayerCard() {
        String[] names = new String[]{
            "Follow Me", "Can't Catch Me", "Running in Fear",
            "CowardBot", "BraveBot", "POOL SHARK", "LOAN SHARK",
            "Rye Bread", "White Bread", "CheatBot", "Little Bit",
            "CAN YOU NOT", "Just a Byte?", "Useless", "AttackBot",
            "Bad Spelling", "Dr. Bot", "Sir Bot", "Madame Bot", "Mr. Bot",
            "Mrs. Bot", "Bot Jr.", "Old Man", "The 107th Doctor", "No Name",
            "bot.exe", "bot.jpg", "RaNuGe", "Stay Away", "Jeb Bush", "*loud noises*",
            "Mild Dip", "Milquetoast", "Weakling", "SHARK SHARK", "All Good, Man",
            "2% Milk", "THIS ETHAN", "GONE NUCLEAR", "Kill Me", "A 90's Bot", "Anti-Aliasing",
            "Future You", "LWJGL is Fun", "Humble Harold", "Slick", "Gooey", "Walt",
            "Hank", "Bleep Bloop"
        };
        while (true) {
            int r = Math.abs(new Random().nextInt() % names.length);
            this.name = names[r];
            boolean duplicate = false;
            for (PlayerCard p: GameScreen.PLAYERS) {
                if (p.getName().equals(names[r])) {
                    duplicate = true;
                }
            }
            if (!duplicate) break;
        }
        randomizePersonality();
        this.host = false;
        this.bot = true;
        this.powerups = new int[]{-1, -1, -1, -1};
        this.pressed_keys = new LinkedList<Integer>();
        this.window_dims = new int[]{10000, 10000};
        this.visible_entities = new LinkedList<Entity>();
    }
    
    public final void randomizePersonality() {
        int r = Math.abs(new Random().nextInt() % 3);
        if (r == AI.DEFENSE) bot_ai = new DefenseAI(entity);
        if (r == AI.OFFENSE) bot_ai = new OffenseAI(entity);
        if (r == AI.SCAVENGER) bot_ai = new ScavengerAI(entity);
    }
    
    /**
     * The method that controls bot behavior.
     */
    public final void update() {
        if (bot_ai == null) return;
        bot_ai.update();
    }
    
    public boolean isBot() {
        return bot;
    }
    
    public int powerupCount() {
        int count = 0;
        for (int i = 0; i != powerups.length; i++) {
            if (powerups[i] > -1) count++;
        }
        return count;
    }
    
    public boolean entityEnteredView(Entity e) {
        if (isBot()) return false;
        boolean prev = visible_entities.contains(e);
        boolean now = updateCanSee(e);
        return (prev == false && now == true);
    }
    
    boolean updateCanSee(Entity e) {
        if (isBot()) return true;
        boolean vis = canSee(e);
        if (vis) {
            if (!visible_entities.contains(e)) {visible_entities.add(e);}
        } else {
            visible_entities.remove(e);
        }
        return visible_entities.contains(e);
    }
    
    public void spawnPowerup(int index) {
        Entity e = null;
        int id = getPowerup(index);
        if (id == PowerUp.ENERGY_BOMB) e = Entity.create("energybomb", -1);
        if (id == PowerUp.GRAV_BOMB) e = Entity.create("gravbomb", -1);
        if (id == PowerUp.SIDEKICK) e = Entity.create("sidekick", -1);
        if (id == PowerUp.TURRET) e = Entity.create("turret", -1);
        if (id == PowerUp.TELEPORTER) e = Entity.create("teleporter", -1);
        if (id == PowerUp.FORCE_FIELD) e = Entity.create("forcefield", -1);
        if (e != null) {
            e.setX(getEntity().getWorldCoords()[0]);
            e.setY(getEntity().getWorldCoords()[1]);
            e.setTeam(getTeam());
            addEntity(e, false, false);
            removePowerup(index);
        }
    }
    
    public boolean canSee(Entity e) {
        if (e == null || entity == null) return false;
        int w = window_dims[0]+100+(e.getSize()*2);//+(e.getRangeIndicatorSize()*2);
        int h = window_dims[1]+100+(e.getSize()*2);//+(e.getRangeIndicatorSize()*2);
        double x = entity.getWorldCoords()[0]-(w/2);
        double y = entity.getWorldCoords()[1]-(h/2);
        boolean vis = MiscMath.rectanglesIntersect(x, y, w, h, 
                (int)e.getWorldCoords()[0]-e.getSize(), (int)e.getWorldCoords()[1]-e.getSize(),
                e.getSize(), e.getSize());
        return vis || e.equals(entity);
    }
    
    public void setWindowDimensions(int w, int h) {
        window_dims = new int[]{w, h};
    }
    
    public boolean hasPowerup(int id) {
        for (int i = 0; i != powerups.length; i++) {
            if (powerups[i] == id) return true;
        }
        return false;
    }
    
    public int getPowerup(int index) {
        if (index < 0 || index >= powerups.length) return -1;
        return powerups[index];
    }
    
    public void setConnection(Connection c) {
        conn = c;
    }
    
    public void setBot(boolean b) {
        bot = b;
    }
    
    public boolean isKeyPressed(int key) {
        return pressed_keys.contains(key);
    }
    
    public void addPressedKey(int key) {
        if (pressed_keys.contains(key) == false) {
            pressed_keys.add(key);
        }
    }
    
    public void removePressedKey(int key) {
        if (!pressed_keys.contains(key)) return;
        pressed_keys.remove(pressed_keys.indexOf(key));
    }
    
    public boolean isHost() {
        return host;
    }
    
    public void toggleTeam() {
        if (team.team < 3) {
            setTeam(GameScreen.AVAILABLE_TEAMS[team.team+1]);
        } else {
            setTeam(GameScreen.AVAILABLE_TEAMS[0]);
        }
    }
    
    public void setHost(boolean h) {
        host = h;
    }
    
    public String getName() {
        return name;
    }
    
    public Connection getConnection() {
        return conn;
    }
    
    public void reset() {
        this.powerups = new int[]{-1, -1, -1, -1};
        this.pressed_keys.clear();
    }
    
    public void addPowerup(int type) {
        if (type < 0) return;
        for (int i = 0; i != powerups.length; i++) {
            if (powerups[i] == -1) {
                powerups[i] = type;
                break;
            }
        }
        if (this.equals(GameScreen.YOU)) {
            GameScreen.notification(PowerUp.NAMES[type]+" acquired!", Color.lightGray);
        }
        if (GameScreen.YOU.isHost()) {
            UpdatePowerupList u = new UpdatePowerupList();
            u.add_powerup_id = type;
            if (conn != null) {
                MPServer.sendToPlayer(this, u, false);
            }
        }
    }
    
    public void setName(String n) {
        name = n;
    }
    
    public void removePowerup(int index) {
        if (index <= -1 || index >= powerups.length) return;
        powerups[index] = -1;
        if (GameScreen.YOU.isHost()) {
            UpdatePowerupList u = new UpdatePowerupList();
            u.remove_powerup_index = index;
            if (conn != null) {
                MPServer.sendToPlayer(this, u, false);
            }
        }
    }
    
    public TeamCard getTeam() {
        return team;
    }
    
    public Player getEntity() {
        return entity;
    }
    
    public void setEntity(Player e) {
        entity = e;
        if (bot_ai != null) bot_ai.parent = e;
        entity.setOwner(this);
        Camera.setTarget(e);
    }
    
    public int getEnergy() {
        if (entity == null) return -1;
        return (int)entity.getEnergy();
    }
    
    public void setTeam(TeamCard t) {
        if (team != null) team.removeMember(this);
        team = t;
        team.addMember(this);
        System.out.println(this.getName()+"'s team is now "+t.getName());
    }
    
}

class AI {
    
    public static int DEFENSE = 0, OFFENSE = 1, SCAVENGER = 2;
    Player parent;
    Entity target_orb = null;
    boolean drain = false;
    double shoot_timer = 0, flee_timer = 0, powerup_timer = 10+Math.abs(new Random().nextInt() % 10),
            follow_timer = 0;
    double[] search_region = new double[]{0, 0, 0};
    
    public void update() {
        Entity sighted = closestEnemy(1600);
        boolean has_part = parent.getParts() > 0;
        HomeBase home_base = parent.getTeam().getHomeBase();
        HomeBase enemy_base = closestEnemyHomeBase();
        boolean reacting_to_entity = false;
        if (sighted != null && !has_part) {
            if (parent.getEnergy() >= sighted.getEnergy() + 30) {
                if (sighted.distanceTo(parent) < 800 || !(sighted instanceof Player)) { 
                    shoot(sighted);
                    if (sighted instanceof Player && sighted.distanceTo(parent) > 400) {
                        follow(sighted);
                    }
                    reacting_to_entity = true;
                } else {
                    parent.setVelocity(0, 0, 0, 0, true);
                }
            } else {
                if (sighted.distanceTo(parent) < 800) {
                    fleeFrom(sighted);
                    reacting_to_entity = true;
                } else {
                    parent.setVelocity(0, 0, 0, 0, true);
                }
            }
            //place powerups! :D
            for (int i = 0; i != parent.getOwner().powerupCount() && sighted.distanceTo(parent) <= 800; i++) {
                if (i >= parent.getOwner().powerupCount()) break;
                int id = parent.getOwner().getPowerup(i);
                if (id == PowerUp.TURRET) {
                    placePowerUp(i);
                } else if (id == PowerUp.ENERGY_BOMB) {
                    if (sighted.getEnergy() > 20) {
                        placePowerUp(i);
                    }
                } else if (id == PowerUp.GRAV_BOMB) {
                    if (sighted.getEnergy() > parent.getEnergy()
                            || sighted.getOrbs() > 5) {
                        placePowerUp(i);
                    }
                } else if (id == PowerUp.TELEPORTER) {
                    if (parent.getEnergy() < 20 || parent.getOrbs() > 12) {
                        placePowerUp(i);
                    }
                } else if (id == PowerUp.FORCE_FIELD) {
                    if (parent.getEnergy() < 40) {
                        placePowerUp(i);
                    }
                } else if (id == PowerUp.SIDEKICK) {
                    placePowerUp(i);
                }
            }
        }
        if (has_part) {
            parent.setVelocityFor(home_base.getWorldCoords()[0], home_base.getWorldCoords()[1], 0, 0, 110, true);
            if (parent.distanceTo(home_base) < 30) {
                parent.setVelocity(0, 0, 0, 0, true);
            }
            return;
        }
        if (enemy_base != null) {
            if (enemy_base.distanceTo(parent) < 1000) {
                shoot(enemy_base);
                if (enemy_base.getEnergy() < 20) {
                    parent.setVelocityFor(enemy_base.getWorldCoords()[0], enemy_base.getWorldCoords()[1], 0, 0, 110, true);
                    if (parent.distanceTo(enemy_base) < 30) {
                        parent.setVelocity(0, 0, 0, 0, true);
                    }
                }
            }
        }
        //if (!reacting_to_entity) {
            System.out.println(parent.getOwner().getName()+" not reacting to entity");
            if (parent.getTeam().getPartCount() >= GameScreen.getActiveTeams().size()) {
                scavenge();
            } else {
                searchForParts();
            }
        //}
        if (home_base.getEnergy() < 60 
                && home_base.distanceTo(parent) < home_base.getPullRadius()
                && home_base.distanceTo(parent) > 40) {
            parent.drainEnergy(home_base, 1);
        }
    }
    
    /**
     * Searches 3 locations for parts: the open world, players, and then home bases, in that order.
     * When searching home bases, they will shoot more aggressively at them, and when home base is at 0, they will go close to it.
     */
    public void searchForParts() {
        
        Entity closest_part = null;
        double shortest_part_dist = 1000000;
        for (int i = 0; i != GameScreen.GAME_OBJECTS.size(); i++) {
            Entity p = GameScreen.GAME_OBJECTS.get(i);
            if (p instanceof CollectorPart) {
                if (parent.distanceTo(p) < shortest_part_dist) {
                    closest_part = p;
                    shortest_part_dist = parent.distanceTo(p);
                }
            }
        }
        
        if (closest_part != null) {
            parent.setVelocityFor(closest_part.getWorldCoords()[0], closest_part.getWorldCoords()[1], 0, 0, 110, true);
          
            return;
        }
        
        Player closest = null;
        double shortest_dist = 1000000;
        for (int i = 0; i != GameScreen.PLAYERS.size(); i++) {
            Player p = GameScreen.PLAYERS.get(i).getEntity();
            if (p.getTeam().getTeamCode() != parent.getTeam().getTeamCode()) {
                if (p.getParts() > 0) {
                    if (parent.distanceTo(p) < shortest_dist) {
                        closest = p;
                        shortest_dist = parent.distanceTo(p);
                    }
                }
            }
        }
        
        if (closest != null) {
            shoot(closest);
            if (closest instanceof Player && closest.distanceTo(parent) > 400) {
                follow(closest);
            }
            return;
        }
        
        HomeBase hb = getHomeBaseWithMostParts();
        if (hb != null) {
            parent.setVelocityFor(hb.getWorldCoords()[0], hb.getWorldCoords()[1], 0, 0, 110, true);
            if (parent.distanceTo(hb) < 40) {
                parent.setVelocity(0, 0, 0, 0, true);
            }
        }
    }
    
    public HomeBase getHomeBaseWithMostParts() {
        HomeBase highest = null;
        double partcount = 0;
        for (int i = 0; i != GameScreen.AVAILABLE_TEAMS.length; i++) {
            HomeBase p = GameScreen.AVAILABLE_TEAMS[i].getHomeBase();
            if (p == null) continue;
            if (p.getTeam().getTeamCode() != parent.getTeam().getTeamCode()) {
                if (p.getTeam().getPartCount() > partcount) {
                    highest = p;
                    partcount = p.getTeam().getPartCount();
                }
            }
        }
        return highest;
    }
    
    public void setSearchRegion(double x, double y, double rad) {
        search_region = new double[]{x, y, rad};
    }
    
    public void follow(Entity e) {
        follow_timer -= MiscMath.getConstant(10, 1);
        target_orb = null;
        parent.addEnergy(-MiscMath.getConstant(10, 1));
        if (follow_timer > 0) return;
        follow_timer = 0.6;
        double random_x = e.getWorldCoords()[0]-parent.getWorldCoords()[0];
        double random_y = e.getWorldCoords()[1]-parent.getWorldCoords()[1];
        parent.setVelocityFor(e.getWorldCoords()[0], e.getWorldCoords()[1], 0, 0, 200, true);
    }
    
    public void fleeFrom(Entity e) {
        flee_timer -= MiscMath.getConstant(10, 1);
        target_orb = null;
        parent.addEnergy(-MiscMath.getConstant(10, 1));
        if (flee_timer > 0) return;
        flee_timer = 3;
        double random_x = e.getWorldCoords()[0]-parent.getWorldCoords()[0];
        double random_y = e.getWorldCoords()[1]-parent.getWorldCoords()[1];
        parent.setVelocityFor(parent.getWorldCoords()[0]-random_x, parent.getWorldCoords()[1]-random_y, 0, 0, 200, true);
    }
    
    public void shoot(Entity e) {
        shoot_timer -= MiscMath.getConstant(5, 1);
        if (shoot_timer >= 0) return;
        shoot_timer = 0.5;
        if (parent.getEnergy() < 10) return;
        Entity p = Entity.create("projectile", -1);
        double point[] = parent.getRotatedPoint(0, parent.getSize() + 25, (int)MiscMath.angleBetween
        (parent.getWorldCoords()[0], parent.getWorldCoords()[1], e.getWorldCoords()[0], e.getWorldCoords()[1]));
        p.setX(point[0]);
        p.setY(point[1]);
        p.setTeam(parent.getTeam());
        p.setVelocityFor(e.getWorldCoords()[0], e.getWorldCoords()[1], 0, 0, 350, false);
        GameScreen.addEntity(p, false, false);
        parent.addEnergy(-5);
        System.out.println("Shot "+e.getSpawnCode());
    }
    
    public void scavenge() {
        if (!drain) {
            if (target_orb == null) {
                double short_dist = 200000000;
                LinkedList<Entity> all_entities = new LinkedList<Entity>();
                all_entities.addAll(GameScreen.ORBS);
                if (parent.getOwner().powerupCount() < PlayerCard.POWERUP_LIMIT) all_entities.addAll(GameScreen.GAME_OBJECTS);
                for (int i = 0; i != all_entities.size(); i++) {
                    if (i >= all_entities.size()) break;
                    Entity e = all_entities.get(i);
                    if (e != null) {
                        if (!(e instanceof Orb || e instanceof PowerUp)) continue;
                        double dist = MiscMath.distanceBetween(parent.getWorldCoords()[0], parent.getWorldCoords()[1], 
                                e.getWorldCoords()[0], e.getWorldCoords()[1]);
                        if (search_region[2] == 0 || MiscMath.pointIntersects(e.getWorldCoords()[0], e.getWorldCoords()[1], 
                                search_region[0]-search_region[2], search_region[1]-search_region[2], (int)search_region[2]*2, (int)search_region[2]*2)) {
                            if (dist < 1000) {
                                if (Math.abs(new Random().nextInt() % 8) == 0) { target_orb = e; break; }
                            }
                        }
                    }
                }
            }

            if (target_orb != null) {
                parent.setVelocityFor(target_orb.getWorldCoords()[0], target_orb.getWorldCoords()[1], 0, 0, 100, true);
                if (!(GameScreen.ORBS.contains(target_orb) || GameScreen.GAME_OBJECTS.contains(target_orb))) target_orb = null;
            }
        } else {
            Entity hb = parent.getTeam().getHomeBase();
            if (hb != null) {
                if (hb.distanceTo(parent) <= 100) {
                    parent.setVelocityFor(0, 0, 0, 0, 0, true);
                } else {
                    parent.setVelocityFor(hb.getWorldCoords()[0], hb.getWorldCoords()[1], 0, 0, 170, true);
                }
            }
        }
        
        if (parent.getOrbs() <= 0 && drain) {
            drain = false;
        }
        if (!drain && parent.getOrbs() >= 15) {
            drain = true;
            target_orb = null;
        }
        //if no orbs to be found
        if (target_orb == null && drain == false) {
            parent.setVelocity(0, 0, 0, 0, true);
        }
    }
    
    public void placePowerUp(int index) {
        powerup_timer -= MiscMath.getConstant(5, 1);
        if (powerup_timer > 0) return;
        powerup_timer = 10+Math.abs(new Random().nextInt() % 10);
        parent.getOwner().spawnPowerup(index);
    }
    
    public Entity closestEnemy(int distance) {
        Entity closest = null;
        double short_dist = distance;
        for (int i = 0; i != GameScreen.GAME_OBJECTS.size(); i++) {
            Entity p = GameScreen.GAME_OBJECTS.get(i);
            if (p instanceof PowerUp || p instanceof EnergyBombProjectile) continue;
            if (p.distanceTo(parent) < distance
                    && p.getTeam().getTeamCode() != parent.getTeam().getTeamCode()) {
                if (p.distanceTo(parent) < short_dist) {
                    closest = p;
                    short_dist = p.distanceTo(parent);
                }
            }
        }
        return closest;
    }
    
     public HomeBase closestEnemyHomeBase() {
        HomeBase closest = null;
        double short_dist = 100000000;
        for (int i = 0; i != GameScreen.AVAILABLE_TEAMS.length; i++) {
            HomeBase p = GameScreen.AVAILABLE_TEAMS[i].getHomeBase();
            if (p == null) continue;
            if (p.getTeam().getTeamCode() != parent.getTeam().getTeamCode()) {
                if (p.distanceTo(parent) < short_dist) {
                    closest = p;
                    short_dist = p.distanceTo(parent);
                }
            }
        }
        return closest;
    }
    
    public Player closestEnemyPlayer(int distance) {
        Player closest = null;
        double short_dist = distance;
        for (int i = 0; i != GameScreen.PLAYERS.size(); i++) {
            Player p = GameScreen.PLAYERS.get(i).getEntity();
            if (p.distanceTo(parent) < distance
                    && p.getTeam().getTeamCode() != parent.getTeam().getTeamCode()) {
                if (p.distanceTo(parent) < short_dist) {
                    closest = p;
                    short_dist = p.distanceTo(parent);
                }
            }
        }
        return closest;
    }
    
}

class DefenseAI extends AI {
    
    public DefenseAI(Player player) {parent = player;}

    @Override
    public void update() {
        super.update();
        HomeBase home_base = parent.getTeam().getHomeBase();
        setSearchRegion(home_base.getWorldCoords()[0], home_base.getWorldCoords()[1], 27*64);
    }
    
}

class ScavengerAI extends AI {

    public ScavengerAI(Player player) {parent = player;}
    
    @Override
    public void update() {
        super.update();
        setSearchRegion(0, 0, 0);
    }
    
}

class OffenseAI extends AI {

    public OffenseAI(Player player) {parent = player;}
    
    @Override
    public void update() {
        super.update();
        TeamCard most_parts = null;
        int highest_parts = 0;
        for (int i = 0; i != GameScreen.AVAILABLE_TEAMS.length; i++) {
            TeamCard t = GameScreen.AVAILABLE_TEAMS[i];
            if (t.getPartCount() > highest_parts && parent.getTeam().getTeamCode() != t.getTeamCode()) {
                most_parts = t;
                highest_parts = t.getPartCount();
            }
        }
        if (most_parts != null) {
            HomeBase home_base = most_parts.getHomeBase();
            setSearchRegion(home_base.getWorldCoords()[0], home_base.getWorldCoords()[1], 46*64);
        } else {
            setSearchRegion(0, 0, 0);
        }
    }
    
}
