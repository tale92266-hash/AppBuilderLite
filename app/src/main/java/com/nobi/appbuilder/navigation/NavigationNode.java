package com.nobi.appbuilder.navigation;

/**
 * One screen in the navigation graph, keyed by its stable screen id
 * — never by display name, which the user can rename.
 */
public class NavigationNode {

    private String screenId;
    private String name;
    private boolean launcher;
    private float x;
    private float y;

    public NavigationNode() {
    }

    public NavigationNode(String screenId, String name, boolean launcher,
                          float x, float y) {
        this.screenId = screenId;
        this.name = name;
        this.launcher = launcher;
        this.x = x;
        this.y = y;
    }

    public String getScreenId() {
        return screenId;
    }

    public void setScreenId(String screenId) {
        this.screenId = screenId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLauncher() {
        return launcher;
    }

    public void setLauncher(boolean launcher) {
        this.launcher = launcher;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
