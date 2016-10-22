package com.bitbucket.computerology.world;

import com.bitbucket.computerology.misc.MiscMath;

public class Force {
    
    double x_mag, y_mag, orig_x_mag, orig_y_mag, x_acc, y_acc;
    int lifespan = 0; //in frames
    
    public Force(double xm, double ym, double xa, double ya) {
        this.x_mag = xm;
        this.y_mag = ym;
        this.x_acc = xa;
        this.y_acc = ya;
        this.orig_x_mag = xm;
        this.orig_y_mag = ym;
    }
    
    public void update() {
        if (x_acc >= 0) {
            if (x_mag > 0) {
                x_mag+=MiscMath.getConstant(Math.abs(x_acc)*60, 1);
            }
            if (x_mag < 0) {
                x_mag-=MiscMath.getConstant(Math.abs(x_acc)*60, 1);
            }
        } else if (x_acc < 0) {
            if (x_mag > 0) {
                x_mag-=MiscMath.getConstant(Math.abs(x_acc)*60, 1);
            }
            if (x_mag < 0) {
                x_mag+=MiscMath.getConstant(Math.abs(x_acc)*60, 1);
            }
        }
        if (y_acc >= 0) {
            if (y_mag > 0) {
                y_mag+=MiscMath.getConstant(Math.abs(y_acc)*60, 1);
            }
            if (y_mag < 0) {
                y_mag-=MiscMath.getConstant(Math.abs(y_acc)*60, 1);
            }
        } else if (y_acc < 0) {
            if (y_mag > 0) {
                y_mag-=MiscMath.getConstant(Math.abs(y_acc)*60, 1);
            }
            if (y_mag < 0) {
                y_mag+=MiscMath.getConstant(Math.abs(y_acc)*60, 1);
            }
        }
        if (Math.abs(x_mag) <= 1) x_mag = 0;
        if (Math.abs(y_mag) <= 1) y_mag = 0;
        lifespan++;
    }
    
    public void setXAcceleration(int x) {
        x_acc = x;
    }
    
    public void setYAcceleration(int y) {
        y_acc = y;
    }
    
    public double getXMagnitude() {
        return x_mag;
    }
    
    public double getYMagnitude() {
        return y_mag;
    }
    
    public double getXAcceleration() {
        return x_acc;
    }
    
    public double getYAcceleration() {
        return y_acc;
    }
    
    public void setYMagnitude(int mag) {
        y_mag = mag;
    }
    
    public void setXMagnitude(int mag) {
        x_mag = mag;
    }
    
    public void reset() {
        lifespan = 0;
        x_mag = orig_x_mag;
        y_mag = orig_y_mag;
    }
    
}
