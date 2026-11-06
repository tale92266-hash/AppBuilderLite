package com.nobi.appbuilder.library.design;

/** A named corner-radius value in dp. */
public class ShapeToken {

    private final String name;
    private int cornerRadiusDp;

    public ShapeToken(String name, int cornerRadiusDp) {
        this.name = name;
        this.cornerRadiusDp = cornerRadiusDp;
    }

    public String getName() {
        return name;
    }

    public int getCornerRadiusDp() {
        return cornerRadiusDp;
    }

    public void setCornerRadiusDp(int cornerRadiusDp) {
        this.cornerRadiusDp = cornerRadiusDp;
    }

    public DesignToken toToken(String id, String projectId) {
        return new DesignToken(id, projectId, DesignToken.KIND_SHAPE,
                name, String.valueOf(cornerRadiusDp));
    }

    public static ShapeToken fromToken(DesignToken token) {
        int radius = 8;
        try {
            radius = Integer.parseInt(token.getValue());
        } catch (NumberFormatException ignored) {
        }
        return new ShapeToken(token.getName(), radius);
    }
}
