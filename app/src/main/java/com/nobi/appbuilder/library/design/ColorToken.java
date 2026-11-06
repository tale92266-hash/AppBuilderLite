package com.nobi.appbuilder.library.design;

/** A named colour value, e.g. Primary #0B4499. */
public class ColorToken {

    private final String name;
    private String hex;

    public ColorToken(String name, String hex) {
        this.name = name;
        this.hex = hex;
    }

    public String getName() {
        return name;
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

    public DesignToken toToken(String id, String projectId) {
        return new DesignToken(id, projectId, DesignToken.KIND_COLOR,
                name, hex);
    }

    public static ColorToken fromToken(DesignToken token) {
        return new ColorToken(token.getName(), token.getValue());
    }
}
