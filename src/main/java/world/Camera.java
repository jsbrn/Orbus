package world;

import world.entities.Entity;

public class Camera {
    public static double WORLD_X = 0, WORLD_Y = 0;
    public static double SPEED = 0.1;
    static Entity target = null;
    static int[] target_point = new int[]{0, 0};
    
    public static void move(double x, double y) {
        WORLD_X += x;
        WORLD_Y += y;
    }
    
    public static void setTarget(Entity o) {
        target = o;
    }
    
    public static void setTarget(int w_x, int w_y) {
        target_point[0] = w_x; target_point[1] = w_y;
    }
    
    public static void update() {
        if (target == null) return;
        WORLD_X += (target.getWorldCoords()[0]-WORLD_X)*(SPEED);
        WORLD_Y += (target.getWorldCoords()[1]-WORLD_Y)*(SPEED);
    }
    
}
