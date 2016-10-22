package com.bitbucket.computerology.misc;

import com.bitbucket.computerology.gui.GameScreen;
import org.newdawn.slick.Input;

/**
 * UserInput is a convenient wrapper for Slick2D's input methods and classes.
 */
public class UserInput {
    
    /**
     * Returns true if the mouse button specified is being held down.
     * Respects LCONTROL+M1 as a right click alternative.
     * @param button The Slick2D id number for the button.
     * @return A boolean indicating if the mouse button is being held down.
     */
    public static boolean mouseButtonDown(int button) {
        //if button is 0 (left mouse button), return false if LCONTROL is held (LCONTROL is reserved for right click)
        if (button == 0) {
            if (GameScreen.getInput().isKeyDown(Input.KEY_LCONTROL)) {
                return false;
            }
        }
        return GameScreen.getInput().isMouseButtonDown(button);
    }
    
    /**
     * Returns true if the mouse button specified was pressed (not held). 
     * Respects LCONTROL+M1 as a right click alternative.
     * @param button The Slick2D id number for the button.
     * @return A boolean indicating if the mouse button was pressed.
     */
    public static boolean mouseButtonPressed(int button) {
        if (button == 1) {
            return rightClick();
        } else if (button == 0) {
            return leftClick();
        } else {
            return GameScreen.getInput().isMousePressed(button);
        }
    }
    
    /**
     * Returns true if the user right clicks.
     * Respects LCONTROL+M1 as a right click alternative.
     * @return A boolean indicating if the mouse was right clicked.
     */
    public static boolean rightClick() {
        return GameScreen.getInput().isMousePressed(1) 
                || (GameScreen.getInput().isMousePressed(0) && GameScreen.getInput().isKeyDown(Input.KEY_LCONTROL));
    }
    
    /**
     * Returns true if the user left clicks.
     * Respects LCONTROL+M1 as a right click alternative.
     * @return A boolean indicating if the mouse was left clicked.
     */
    public static boolean leftClick() {
        if (GameScreen.getInput().isKeyDown(Input.KEY_LCONTROL) == false) {
            return GameScreen.getInput().isMousePressed(0);
        }
        return false;
    }

}
