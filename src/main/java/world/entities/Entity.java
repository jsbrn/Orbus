package world.entities;

import gui.GameScreen;
import misc.MiscMath;
import network.Packet.*;
import network.server.MPServer;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import world.Camera;
import world.Force;
import world.PlayerCard;
import world.TeamCard;
import world.entities.deployables.*;
import world.entities.homebase.HomeBase;
import world.entities.orbs.EnergyOrb;
import world.entities.orbs.Orb;
import world.entities.part.CollectorPart;
import world.entities.player.Player;
import world.entities.powerups.PowerUp;
import world.entities.projectiles.EnergyBombProjectile;

import java.util.LinkedList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Entity {
    
    double energy = 100, world_x, world_y;
    int radius = 32, range = 32; //the radius of the entity (not the gravity radius)
    LinkedList<Force> forces = new LinkedList<Force>();
    Force MOVEMENT;
    TeamCard team;
    int r_i_radius = 0;
    
    double ae_timer = 10;
    
    Image orb, texture, part;
    static Image[] RANGE_INDICATORS;
    
    double energy_drain_timer = 1, orb_drain_timer = 1, part_drain_timer = 1;
    String name, texture_url = "";
    int id = -1, orbs = 0, parts = 0;
    double energy_at_last_packet = 0;
    double orb_render_angle = 0, part_render_angle = 0;
    
    public void update(){}
    
    public void draw(Graphics g) {
        if (RANGE_INDICATORS == null) {
            RANGE_INDICATORS = new Image[4];
            try {
                RANGE_INDICATORS[0] = new Image("images/redrange.png");
                RANGE_INDICATORS[1] = new Image("images/bluerange.png");
                RANGE_INDICATORS[2] = new Image("images/greenrange.png");
                RANGE_INDICATORS[3] = new Image("images/purplerange.png");
            } catch (SlickException ex) {
                Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (texture == null) { 
            try { 
                texture = new Image("images/orb.png");
                orb = new Image("images/orb.png", false, Image.FILTER_LINEAR);
                part = new Image("images/collectorpart.png", false, Image.FILTER_LINEAR);
            } catch (SlickException ex) {
                Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        if (texture_url != null) {
            if (texture_url.contains(texture.getResourceReference()) == false) {
                try {
                    texture = new Image(texture_url, false, Image.FILTER_NEAREST);
                } catch (SlickException ex) {
                    Logger.getLogger(Entity.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public double getPull(double distance) {
        return 75*(energy/(2*distance));
    }
    
    public int getPullRadius() {
        return 75*(int)(energy/20);
    }
    
    public void setTexture(String url) {
        texture_url = url;
    }
    
    public int getRangeIndicatorSize() {
        return r_i_radius;
    }
    
    public Image getTexture() {
        return texture;
    }
    
    public void onSpawn() {
        
    }
    
    public void setSize(int r) {
        radius = r;
    }
    
    public void setRange(int r) {
        range = r;
    }
    
    public int getRange() {
        return range;
    }
    
    public TeamCard getTeam() {
        return team;
    }
    
    public void setTeam(TeamCard t) {
        if (team != null) team.removeEntity(this);
        team = t;
        team.addEntity(this);
    }
    
    public String getSpawnCode() {
        return name;
    }
    
    public int getSize() {
        return radius;
    }
    
    public void drawOrbs(Graphics g) {
        if (orbs <= 0) return;
        orb_render_angle+=MiscMath.getConstant(30, 1);
        orb_render_angle %= 360;
        int count = orbs; if (count > 50) count = 50;
        for (int i = 0; i != count; i++) {
            double[] point = getRotatedOffset(0, getSize()+15, (int)orb_render_angle+(15*i));
            g.setColor(Color.white);
            g.drawImage(orb, (float)getOnscreenCoords()[0]+(float)point[0]-10, 
                    (float)getOnscreenCoords()[1]+(float)point[1]-10);
        }
    }
    
    public void drawParts(Graphics g) {
        if (parts <= 0) return;
        part_render_angle-=MiscMath.getConstant(30, 1);
        part_render_angle %= 360;
        int count = parts;
        for (int i = 0; i != count; i++) {
            double[] point = getRotatedOffset(0, getSize()+60, (int)part_render_angle+(40*i));
            g.setColor(Color.white);
            g.drawImage(part, (float)getOnscreenCoords()[0]+(float)point[0]-33, 
                    (float)getOnscreenCoords()[1]+(float)point[1]-33);
        }
    }
    
    public void drainNearbyEnemies() {
        for (int i = 0; i != GameScreen.GAME_OBJECTS.size(); i++) {
            if (i >= GameScreen.GAME_OBJECTS.size()) break;
            Entity e = GameScreen.GAME_OBJECTS.get(i);
            if (e != null) {
                if (e instanceof GravBomb == false) {
                    if (e.getTeam().equals(this.getTeam()) == false) {
                        if (MiscMath.distanceBetween(getWorldCoords()[0], getWorldCoords()[1], 
                                e.getWorldCoords()[0], e.getWorldCoords()[1]) < getPullRadius()) {
                            if (getPullRadius() > e.getPullRadius()) {
                                e.drainOrbs(this, 1);
                            }
                        }
                    }
                }
            }
        }
    }
    
    public final void dropGoodies() {
        if (!GameScreen.YOU.isHost()) return;
        System.out.println("Dropping goodies for "+getSpawnCode()+this.getID());
        Random r = new Random();
        for (int i = 0; i != orbs; i++) {
            Entity o = Entity.create("orb", -1);
            o.setX(this.getWorldCoords()[0]);
            o.setY(this.getWorldCoords()[1]);
            o.setVelocity(r.nextInt() % 100, r.nextInt() % 100, -2, -2, false);
            GameScreen.addEntity(o, true, false);
        }
        for (int i = 0; i != 5; i++) {
            Entity o = Entity.create("energyorb", -1);
            o.setX(this.getWorldCoords()[0]);
            o.setY(this.getWorldCoords()[1]);
            o.setVelocity(r.nextInt() % 100, r.nextInt() % 100, -2, -2, false);
            GameScreen.addEntity(o, true, false);
        }
        for (int i = 0; i != parts; i++) {
            Entity o = Entity.create("part", -1);
            o.setX(this.getWorldCoords()[0]);
            o.setY(this.getWorldCoords()[1]);
            o.setVelocity(r.nextInt() % 100, r.nextInt() % 100, -2, -2, false);
            GameScreen.addEntity(o, true, false);
        }
    }
    
    public void onDelete() {}
    
    /**
     * Calculates the force needed to move in the direction of the specified point.
     * @param world_x
     * @param world_y
     * @return The force calculated (as a Force object).
     */
    public Force getForceTo(double world_x, double world_y, double magnitude_multiplier) {
        double xdist = this.world_x - world_x;
        double ydist = this.world_y - world_y;

        double atan = Math.atan(xdist/ydist);

        double cos = Math.cos(atan);
        double sin = Math.sin(atan);
        cos *= magnitude_multiplier; sin *= magnitude_multiplier;
        
        Force f = null;
        if (world_y > this.world_y) {
            f = new Force(sin, cos, 0, 0);
        } else {
            f = new Force(-sin, -cos, 0, 0);
        }
        return f;
    }
    
    public void move() {
        if (!GameScreen.YOU.canSee(this) && !GameScreen.YOU.isHost()) GameScreen.deleteEntity(this, true);
        if (!forces.contains(MOVEMENT)) forces.add(MOVEMENT);
        double dx = 0, dy = 0;
        for (int i = 0; i != forces.size(); i++) {
            Force f = forces.get(i);
            f.update();
            dx += f.getXMagnitude();
            dy+=f.getYMagnitude();
        }
        double energy_mult = ((energy+20)/60);
        if (this instanceof Player && getEnergy() <= 50) energy_mult = 1;
        boolean hit_wall = false;
        world_x += MiscMath.getConstant(dx, 1)*energy_mult;
        world_y += MiscMath.getConstant(dy, 1)*energy_mult;
        if (world_x < 0) { world_x = 0; hit_wall = true; }
        if (world_y < 0) { world_y = 0; hit_wall = true; }
        if (world_x > GameScreen.WORLD_WIDTH*64) { world_x = GameScreen.WORLD_WIDTH*64; hit_wall = true; }
        if (world_y > GameScreen.WORLD_HEIGHT*64){ world_y = GameScreen.WORLD_HEIGHT*64; hit_wall = true; }
        
        //projectiles die when they hit walls
        if (hit_wall && this instanceof EnergyBombProjectile) {
            GameScreen.deleteEntity(this, false);
            return;
        }
        
        if (GameScreen.YOU.isHost() == false) return;
        
        for (int i = 0; i != GameScreen.PLAYERS.size(); i++) {
            PlayerCard p = GameScreen.PLAYERS.get(i);
            if (p.isBot()) continue;
            if (p.entityEnteredView(this)) {
                UpdateEntity u = new UpdateEntity();
                u.energy = getEnergy();
                u.id = this.getID();
                u.orb = this instanceof Orb;
                u.orbs = orbs;
                u.x = getWorldCoords()[0];
                u.y = getWorldCoords()[1];
                AddEntity uv = new AddEntity();
                uv.x = getWorldCoords()[0];
                uv.y = getWorldCoords()[1];
                uv.dx = getMovementVector()[0];
                uv.dy = getMovementVector()[1];
                uv.ax = getMovementVector()[2];
                uv.team_code = getTeam().getTeamCode();
                uv.id = id;
                uv.spawn_code = getSpawnCode();
                
                System.out.println("Prepared packets for entity "+this.getSpawnCode()+" ["+this.getID()+"]");
                MPServer.sendToPlayer(p, uv, true);
                MPServer.sendToPlayer(p, u, true);
                if (this instanceof Player) {
                    UpdatePlayerCard upc = new UpdatePlayerCard();
                    upc.card_index = GameScreen.PLAYERS.indexOf(((Player)this).getOwner());
                    upc.entity_id = this.getID();
                    upc.team_code = this.getTeam().getTeamCode();
                    MPServer.sendToPlayer(p, upc, true);
                }
            }
        }
    }
    
    public void drawRangeIndicator(Graphics g, int radius, float alpha) {
        int team = getTeam().getTeamCode();
        Image copy = RANGE_INDICATORS[team].copy();
        copy = copy.getScaledCopy((radius+20)*2, (radius+20)*2);
        copy.setAlpha(alpha);
        g.drawImage(copy, (int)getOnscreenCoords()[0]-radius, (int)getOnscreenCoords()[1]-radius);
        r_i_radius = radius;
    }
    
    public static Entity create(String code, int id) {
        
        Entity e = null;
        if (code.contains("powerup_")) { //do powerup_1 to spawn powerup of type 1
            e = new PowerUp(Integer.parseInt(code.replace("powerup_", "")));
        }
        if (code.equals("player")) {
            e = new Player();
        }
        if (code.equals("orb")) {
            e = new Orb();
        }
        if (code.equals("energyorb")) {
            e = new EnergyOrb();
        }
        if (code.equals("homebase")) {
            e = new HomeBase();
        }
        if (code.equals("gravbomb")) {
            e = new GravBomb();
        }
        if (code.equals("energybomb")) {
            e = new EnergyBomb();
        }
        if (code.equals("turret")) {
            e = new Turret();
        }
        if (code.equals("projectile")) {
            e = new EnergyBombProjectile();
        }
        if (code.equals("sidekick")) {
            e = new Sidekick();
        }
        if (code.equals("teleporter")) {
            e = new Teleporter();
        }
        if (code.equals("forcefield")) {
            e = new ForceField();
        }
        if (code.equals("part")) {
            e = new CollectorPart();
        }
        if (e != null) {
            e.id = id; if (id < 0) e.id = Math.abs(new Random().nextInt());
            e.name = code;
            e.team = GameScreen.AVAILABLE_TEAMS[4]; 
            e.MOVEMENT = new Force(0, 0, 0, 0);
            e.forces.add(e.MOVEMENT); 
        }
        return e;
    }
    
    public double distanceTo(Entity e) {
        return MiscMath.distanceBetween(e.getWorldCoords()[0], e.getWorldCoords()[1], 
                            getWorldCoords()[0], getWorldCoords()[1])-e.getSize()-getSize();
    }
    
    public void addPart() {
        parts++;
        if (!GameScreen.YOU.isHost()) return;
        UpdateEntity u = new UpdateEntity();
        u.energy = getEnergy();
        u.id = this.getID();
        u.orb = this instanceof Orb;
        u.orbs = orbs;
        u.x = getWorldCoords()[0];
        u.y = getWorldCoords()[1];
        u.parts = parts;
        MPServer.sendToAllClients(u, false);
        
    }
    
    public void removePart() {
        parts--;
        if (parts < 0) parts = 0;
        if (!GameScreen.YOU.isHost()) return;
        UpdateEntity u = new UpdateEntity();
        u.energy = getEnergy();
        u.id = this.getID();
        u.orb = this instanceof Orb;
        u.orbs = orbs;
        u.x = getWorldCoords()[0];
        u.y = getWorldCoords()[1];
        u.parts = parts;
        MPServer.sendToAllClients(u, false);
    }
    
    public void setParts(int p) {
        parts = p;
    }
    
    public void addOrb() {
        orbs++;
        if (!GameScreen.YOU.isHost()) return;
        UpdateEntity u = new UpdateEntity();
        u.energy = getEnergy();
        u.id = this.getID();
        u.orb = this instanceof Orb;
        u.orbs = orbs;
        u.x = getWorldCoords()[0];
        u.y = getWorldCoords()[1];
        MPServer.sendToAllClients(u, false);
    }
    
    public void removeOrb() {
        orbs--;
        if (orbs < 0) orbs = 0;
        if (!GameScreen.YOU.isHost()) return;
        UpdateEntity u = new UpdateEntity();
        u.energy = getEnergy();
        u.id = this.getID();
        u.orb = this instanceof Orb;
        u.orbs = orbs;
        u.x = getWorldCoords()[0];
        u.y = getWorldCoords()[1];
        MPServer.sendToAllClients(u, false);
    }
    
    /**
     * Drains the base of orbs, in the direction of e.
     * @param e 
     */
    public void drainOrbs(Entity e, double multiplier) {
        orb_drain_timer -= MiscMath.getConstant(multiplier, 1);
        if (orb_drain_timer > 0) return;
        orb_drain_timer = 0.333;
        if (orbs > 0) {
            removeOrb();
            //spawn a new orb
            Entity o = Entity.create("orb", -1);
            //set x to outside the circle facing the entity
            double point[] = getRotatedPoint(0, getSize() + 25, (int)MiscMath.angleBetween
        (getWorldCoords()[0], getWorldCoords()[1], e.getWorldCoords()[0], e.getWorldCoords()[1]));
            o.setX(point[0]);
            o.setY(point[1]);
            ((Orb)o).enableDespawn();
            o.setVelocityFor(e.getWorldCoords()[0], e.getWorldCoords()[1], -1, -1, 250, false);
            GameScreen.addEntity(o, false, false);
        }
    }
    
    public void drainEnergy(Entity e, double rate_multiplier) {
        energy_drain_timer -= MiscMath.getConstant(rate_multiplier, 1);
        if (energy_drain_timer > 0) return;
        energy_drain_timer = 0.666;
        if ((int)getEnergy() > 0) {
            addEnergy(-10);
            //spawn a new orb
            Entity o = Entity.create("energyorb", -1);
            //set x to outside the circle facing the entity
            double point[] = getRotatedPoint(0, getSize() + 25, (int)MiscMath.angleBetween
        (getWorldCoords()[0], getWorldCoords()[1], e.getWorldCoords()[0], e.getWorldCoords()[1]));
            o.setX(point[0]);
            o.setY(point[1]);
            ((Orb)o).enableDespawn();
            o.setVelocityFor(e.getWorldCoords()[0], e.getWorldCoords()[1], -1, -1, 350, false);
            GameScreen.addEntity(o, false, false);
        }
    }
    
    public int getParts() {
        return parts;
    }
    
    public void drainParts(Entity e, double rate_multiplier) {
        part_drain_timer -= MiscMath.getConstant(rate_multiplier, 2);
        if (part_drain_timer > 0) return;
        part_drain_timer = 1;
        if ((int)parts > 0) {
            removePart();
            //spawn a new part
            Entity o = Entity.create("part", -1);
            //set x to outside the circle facing the entity
            double point[] = getRotatedPoint(0, getSize() + 30, (int)MiscMath.angleBetween
        (getWorldCoords()[0], getWorldCoords()[1], e.getWorldCoords()[0], e.getWorldCoords()[1]));
            o.setX(point[0]);
            o.setY(point[1]);
            o.setVelocityFor(e.getWorldCoords()[0], e.getWorldCoords()[1], -1, -1, 200, false);
            GameScreen.addEntity(o, false, false);
        }
    }
    
    public void setX(double world_x) {
        this.world_x = world_x;
    }
    
    public void setY(double world_y) {
        this.world_y = world_y;
    }
    
    public double getEnergy() {
        return energy;
    }
    
    public void addEnergy(double e) {
        energy+=e;
        if (energy > 300) {
            energy = 300;
        }
        if (energy <= 0) {
            if (GameScreen.YOU.isHost()) {
                if (this instanceof Player) {
                    //teleport the player to home base if they run out of energy
                    Entity en = Entity.create("teleporter", -1);
                    en.setX(getWorldCoords()[0]);
                    en.setY(getWorldCoords()[1]);
                    en.setTeam(getTeam());
                    GameScreen.addEntity(en, false, false);
                    if (((Player)this).getOwner().isBot())
                        ((Player)this).getOwner().randomizePersonality();
                    this.dropGoodies();
                } else if (!(this instanceof HomeBase)) {
                    GameScreen.deleteEntity(this, false);
                    return;
                }
                energy = 0;
                orbs = 0;
                parts = 0;
            }
        }
        if ((int)Math.abs(energy-energy_at_last_packet) >= 0.9 && GameScreen.YOU.isHost()) {
            energy_at_last_packet = energy;
            //send packet
            UpdateEntity u = new UpdateEntity();
            u.id = this.getID();
            u.energy = energy+e;
            u.orb = this instanceof Orb;
            u.orbs = orbs;
            u.x = getWorldCoords()[0];
            u.y = getWorldCoords()[1];
            MPServer.sendToAllClients(u, false);
        }
    }
    
    public void setOrbs(int o) {
        orbs = o;
    }
    
    public int getOrbs() {
        return orbs;
    }
    
    public int getID() {
        return id;
    }
    
    public int[] getMovementVector() {
        return new int[]{(int)MOVEMENT.getXMagnitude(), (int)MOVEMENT.getYMagnitude(),
            (int)MOVEMENT.getXAcceleration(), (int)MOVEMENT.getYAcceleration()};
    }
    
    /**
     * Set the movement vector to (dx, dy, ax, ay). Only sends a packet if the entity is
     * already created. Otherwise, it will not. This is so projectiles that are spawned with
     * a predetermined velocity do not send more than one packet.
     * @param x The x mag.
     * @param y The y mag.
     * @param ax The x acc.
     * @param ay The y acc.
     */
    public void setVelocity(double x, double y, double ax, double ay, boolean send_packet) {
        int old_x = (int)MOVEMENT.getXMagnitude();
        int old_y = (int)MOVEMENT.getYMagnitude();
        int old_xa = (int)MOVEMENT.getXAcceleration();
        int old_ya = (int)MOVEMENT.getYAcceleration();
        if (Math.abs(old_x - x) >= 1 || Math.abs(old_y - y) >= 1 
                || Math.abs(old_xa - ax) >= 1 || Math.abs(old_ya - ay) >= 1) {
            if (GameScreen.YOU.isHost() && send_packet) {
                UpdateMovementVector af = new UpdateMovementVector();
                af.x = this.getWorldCoords()[0];
                af.y = this.getWorldCoords()[1];
                af.dx = x;
                af.dy = y;
                af.ax = ax;
                af.ay = ay;
                af.id = this.getID();
                af.orb = this instanceof Orb;
                MPServer.sendToAllClients(af, false);
            }
        }
        MOVEMENT.setXMagnitude((int)x);
        MOVEMENT.setYMagnitude((int)y);
        MOVEMENT.setXAcceleration((int)ax);
        MOVEMENT.setYAcceleration((int)ay);
    }
    
    public void setVelocityFor(double w_x, double w_y, int ax, int ay, int mag, boolean send_packet) {
        Force f = getForceTo(w_x, w_y, mag);
        setVelocity(f.getXMagnitude(), f.getYMagnitude(), 
                ax, ay, send_packet);
    }
    
    public void addForce(Force f, boolean sendpacket) {
        if (!forces.contains(f)) {
            forces.add(f);
            if (sendpacket) {
                AddForce af = new AddForce();
                af.x = this.getWorldCoords()[0];
                af.y = this.getWorldCoords()[1];
                af.dx = f.getXMagnitude();
                af.dy = f.getYMagnitude();
                af.ax = f.getXAcceleration();
                af.ay = f.getYAcceleration();
                MPServer.sendToAllClients(af, false);
            }
        } else {
            
        }
    }
    
    /**
     * Remove the force at the specified index. Used only for client-side operations, so no packet
     * is sent.
     * @param index lol what do you think this is
     */
    public void removeForce(int index) {
        forces.remove(index);
    }
    
    public void setEnergy(double e) {
        energy = e;
        if (energy < 0) {
            energy = 0;
        }
    }
    
    public void removeForce(Force f, boolean sendpacket) {
        if (forces.contains(f)) {
            if (sendpacket) {
                RemoveForce af = new RemoveForce();
                af.x = this.getWorldCoords()[0];
                af.y = this.getWorldCoords()[1];
                af.findex = forces.indexOf(f);
                af.eid = this.getID();
                MPServer.sendToAllClients(af, false);
            }
            forces.remove(f);
        }
    }
    
    /**
     * Takes an offset from the x and y coordinates of the entity and returns the new offset coordinates, 
     * rotated relative to the entity.
     * @param offset_x The offset on the x axis.
     * @param offset_y The offset on the y axis.
     * @return An int[] of size 2 being {x, y}
     */
    public double[] getRotatedOffset(int offset_x, int offset_y, int rotation) {
        double[] rotated = {-1, -1};
        //P(x, y) = (rsintheta, rcostheta)
        //r being the distance from the center of the entity to the offset point in the original hitbox
        //what this does is essentially take all the points that make up the original hitbox, and use them to
        //find the points after rotation
        double radius = MiscMath.distanceBetween(0, 0, offset_x, offset_y);
        double angle = MiscMath.angleBetween(0, 0, offset_x, offset_y);
        rotated[0] = (radius*(Math.sin(Math.toRadians(-(rotation+angle)))));
        rotated[1] = (radius*(Math.cos(Math.toRadians(-(rotation+angle)))));
        return rotated;
    }
    
    /**
     * Returns the world coordinate of the specified point (offsetx - x, offsety - y) after rotation.
     * @param offset_x The offset x coordinate (from the entity's world x coordinate).
     * @param offset_y Etc.
     * @return 
     */
    public double[] getRotatedPoint(int offset_x, int offset_y, int rotation) {
        offset_y*=-1;
        double[] rotated_point = getRotatedOffset(offset_x, offset_y, rotation);
        rotated_point[0]+=getWorldCoords()[0];rotated_point[1]+=getWorldCoords()[1];
        return rotated_point;
    }
    
    public double[] getWorldCoords() {
        return new double[]{world_x, world_y};
    }
    
    public double[] getOnscreenCoords() {
        double shift_x = (Camera.WORLD_X)-(Display.getWidth()/2), shift_y = (Camera.WORLD_Y)-(Display.getHeight()/2);
        return new double[]{((getWorldCoords()[0])-shift_x), ((getWorldCoords()[1])-shift_y)};
    }
    
    public int[] getRenderCoords() {
        return new int[]{(int)((getOnscreenCoords()[0])-radius), (int)((getOnscreenCoords()[1])-radius)};
    }
    
}
