package com.nobi.appbuilder.library.design;

/** A named spacing value in dp. */
public class SpacingToken {

    private final String name;
    private int dp;

    public SpacingToken(String name, int dp) {
        this.name = name;
        this.dp = dp;
    }

    public String getName() {
        return name;
    }

    public int getDp() {
        return dp;
    }

    public void setDp(int dp) {
        this.dp = dp;
    }

    public DesignToken toToken(String id, String projectId) {
        return new DesignToken(id, projectId, DesignToken.KIND_SPACING,
                name, String.valueOf(dp));
    }

    public static SpacingToken fromToken(DesignToken token) {
        int dp = 8;
        try {
            dp = Integer.parseInt(token.getValue());
        } catch (NumberFormatException ignored) {
        }
        return new SpacingToken(token.getName(), dp);
    }
}
