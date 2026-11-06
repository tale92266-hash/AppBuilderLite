package com.nobi.appbuilder.preview.device;

import java.util.ArrayList;
import java.util.List;

/**
 * The built-in preview device profiles from the spec plus custom
 * sizes typed in by the user. Profiles are immutable value objects;
 * the manager only hands them out.
 */
public class DeviceProfileManager {

    public static final PreviewDeviceProfile COMPACT_PHONE =
            new PreviewDeviceProfile("Compact phone", 360, 800, false);
    public static final PreviewDeviceProfile STANDARD_PHONE =
            new PreviewDeviceProfile("Standard phone", 393, 873, false);
    public static final PreviewDeviceProfile LARGE_PHONE =
            new PreviewDeviceProfile("Large phone", 412, 915, false);
    public static final PreviewDeviceProfile SMALL_TABLET =
            new PreviewDeviceProfile("Small tablet", 600, 960, false);

    private static final List<PreviewDeviceProfile> BUILT_INS = new ArrayList<>();

    static {
        BUILT_INS.add(COMPACT_PHONE);
        BUILT_INS.add(STANDARD_PHONE);
        BUILT_INS.add(LARGE_PHONE);
        BUILT_INS.add(SMALL_TABLET);
    }

    /** All built-in profiles, in the order the dialog shows them. */
    public List<PreviewDeviceProfile> getProfiles() {
        return new ArrayList<>(BUILT_INS);
    }

    public PreviewDeviceProfile getDefault() {
        return STANDARD_PHONE;
    }

    public PreviewDeviceProfile getByName(String name) {
        if (name == null) {
            return getDefault();
        }
        for (PreviewDeviceProfile profile : BUILT_INS) {
            if (profile.getName().equalsIgnoreCase(name)) {
                return profile;
            }
        }
        return getDefault();
    }

    /** Custom size, clamped to sane preview bounds (200–1200 dp). */
    public PreviewDeviceProfile createCustom(int widthDp, int heightDp) {
        widthDp = Math.max(200, Math.min(1200, widthDp));
        heightDp = Math.max(200, Math.min(1200, heightDp));
        return PreviewDeviceProfile.custom(widthDp, heightDp);
    }
}
