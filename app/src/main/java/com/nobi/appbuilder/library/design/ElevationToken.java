package com.nobi.appbuilder.library.design;

/** A named elevation value in dp. */
public class ElevationToken {

    private final String name;
    private int elevationDp;

    public ElevationToken(String name, int elevationDp) {
        this.name = name;
        this.elevationDp = elevationDp;
    }

    public String getName() {
        return name;
    }

    public int getElevationDp() {
        return elevationDp;
    }

    public void setElevationDp(int elevationDp) {
        this.elevationDp = elevationDp;
    }

    public DesignToken toToken(String id, String projectId) {
        return new DesignToken(id, projectId, DesignToken.KIND_ELEVATION,
                name, String.valueOf(elevationDp));
    }

    public static ElevationToken fromToken(DesignToken token) {
        int elevation = 0;
        try {
            elevation = Integer.parseInt(token.getValue());
        } catch (NumberFormatException ignored) {
        }
        return new ElevationToken(token.getName(), elevation);
    }
}
