package com.nobi.appbuilder.assets.colors;

/** A named gradient: type + colours + angle. */
public class GradientModel {

    private String name;
    private String type;
    private int angle;
    private String startColor;
    private String centerColor;
    private String endColor;

    public GradientModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public String getStartColor() {
        return startColor;
    }

    public void setStartColor(String startColor) {
        this.startColor = startColor;
    }

    public String getCenterColor() {
        return centerColor;
    }

    public void setCenterColor(String centerColor) {
        this.centerColor = centerColor;
    }

    public String getEndColor() {
        return endColor;
    }

    public void setEndColor(String endColor) {
        this.endColor = endColor;
    }
}
