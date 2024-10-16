package gui.elements;

import org.newdawn.slick.Graphics;

import java.util.ArrayList;

public class ElementGroup {

    boolean hidden = true;
    ArrayList<GUIElement> children;

    public ElementGroup() {
        children = new ArrayList<GUIElement>();
    }

    public boolean isVisible() {
        return !hidden;
    }

    public void add(GUIElement e) {
        children.add(e);
    }

    public int size() {
        return children.size();
    }
    
    public void setEnabled() {
        for (int i = 0; i != children.size(); i++) {
            children.get(i).setEnabled();
        }
    }
    
    public void setDisabled() {
        for (int i = 0; i != children.size(); i++) {
            children.get(i).setDisabled();
        }
    }

    public void removeAll() {
        children.clear();
    }

    public void remove(GUIElement e) {
        children.remove(e);
    }

    public void remove(int e) {
        children.remove(e);
    }

    public GUIElement get(int e) {
        return children.get(e);
    }

    public void show() {
        hidden = false;
        for (int i = 0; i != children.size(); i++) {
            children.get(i).show();
        }
    }

    public void hide() {
        hidden = true;
        for (int i = 0; i != children.size(); i++) {
            children.get(i).hide();
        }
    }

    public void update() {
        for (int i = 0; i != children.size(); i++) {
            children.get(i).update();
        }
    }

    public void draw(Graphics g) {
        for (int i = 0; i != children.size(); i++) {
            children.get(i).draw(g);
        }
    }

}