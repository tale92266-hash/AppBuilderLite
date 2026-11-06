package com.nobi.appbuilder.preview.device;

import com.nobi.appbuilder.preview.PreviewSession;

/**
 * A design-reference device size in dp. These are preview references,
 * not hardware emulators: the rendered content keeps the project's
 * own dimensions and is simply framed at this size.
 */
public class PreviewDeviceProfile {

    private final String name;
    private final int widthDp;
    private final int heightDp;
    private final boolean custom;

    public PreviewDeviceProfile(String name, int widthDp, int heightDp,
                                boolean custom) {
        this.name = name;
        this.widthDp = widthDp;
        this.heightDp = heightDp;
        this.custom = custom;
    }

    public static PreviewDeviceProfile custom(int widthDp, int heightDp) {
        return new PreviewDeviceProfile("Custom", widthDp, heightDp, true);
    }

    public String getName() {
        return name;
    }

    public int getWidthDp() {
        return widthDp;
    }

    public int getHeightDp() {
        return heightDp;
    }

    public boolean isCustom() {
        return custom;
    }

    /** Width in the given orientation (landscape swaps the sides). */
    public int widthForOrientation(String orientation) {
        return PreviewSession.ORIENTATION_LANDSCAPE.equals(orientation)
                ? heightDp : widthDp;
    }

    public int heightForOrientation(String orientation) {
        return PreviewSession.ORIENTATION_LANDSCAPE.equals(orientation)
                ? widthDp : heightDp;
    }

    /** "Standard phone · 393 × 873 dp" style label for the toolbar. */
    public String getDisplayLabel() {
        return name + " · " + widthDp + " × " + heightDp + " dp";
    }
}
