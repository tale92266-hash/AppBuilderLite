package com.nobi.appbuilder.assets.colors;

/** A named colour resource. */
public class ColorResourceModel {

    private String name;
    private String hex;

    public ColorResourceModel() {
    }

    public ColorResourceModel(String name, String hex) {
        this.name = name;
        this.hex = hex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public boolean isValid() {
        if (hex == null) {
            return false;
        }
        try {
            android.graphics.Color.parseColor(hex);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
