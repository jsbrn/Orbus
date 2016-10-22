package com.bitbucket.computerology.gui.elements;

import com.bitbucket.computerology.gui.GameScreen;
import com.bitbucket.computerology.misc.UserInput;
import com.bitbucket.computerology.misc.Window;
import org.lwjgl.input.Mouse;
import org.newdawn.slick.Graphics;

public class GUIElement {

    double X, Y;
    int W, H;
    boolean hidden = false, enabled = true;

    /**
     * Updates the GUI element (the tasks performed depend on the element type).
     */
    public void update() {
    }

    /**
     * Draws the GUI element to the screen.
     * @param g The graphics instance to draw with.
     */
    public void draw(Graphics g) {
    }
    
    /**
     * Disabled the element and prevents interaction.
     */
    public void disable() {
        enabled = false;
    }

    /**
     * Enabled the element, allowing interaction.
     */
    public void enable() {
        enabled = true;
    }

    /**
     * Make the GUI element visible.
     */
    public void show() {
        hidden = false;
    }

    /**
     * Make the GUI element invisible.
     */
    public void hide() {
        hidden = true;
    }

    /**
     * Set the on-screen x of the GUI element.
     * @param x The new x position.
     */
    public void setX(double x) {
        X = x;
    }

    /**
     * Set the on-screen y of the GUI element.
     * @param y The new y position.
     */
    public void setY(double y) {
        Y = y;
    }

    public double getX() {
        return X;
    }

    public double getY() {
        return Y;
    }
    
    public boolean isClicked(int mouse_button) {
        if (hidden == false && enabled) {
            if (isHovered() && UserInput.mouseButtonPressed(mouse_button)) {
                //GameScreen.getInput().clearKeyPressedRecord();
                return true;
            }
        }
        return false;
    }

    public boolean isHovered() {
        //is mouse hovering over
        if (hidden == false) {
            int mouse_x = Mouse.getX(), mouse_y = Window.getHeight() - Mouse.getY();
            if (mouse_x > getX() && mouse_x < getX() + getWidth()) {
                if (mouse_y > getY() && mouse_y < getY() + getHeight()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Set the width of the GUI element. Some elements, such as buttons, rely on their texture for the width.
     * @param w The new width.
     */
    public void setWidth(int w) {
        W = w;
    }

    /**
     * Set the height of the GUI element. Some elements, such as buttons, rely on their texture for the height.
     * @param h The new height.
     */
    public void setHeight(int h) {
        H = h;
    }

    public int getWidth() {
        return W;
    }

    public int getHeight() {
        return H;
    }

    public boolean isVisible() {
        return !hidden;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled() {
        enabled = true;
    }
    
    public void setDisabled() {
        enabled = false;
    }

}