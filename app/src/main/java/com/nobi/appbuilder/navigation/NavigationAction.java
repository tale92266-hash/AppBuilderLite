package com.nobi.appbuilder.navigation;

/** Navigation actions an edge can carry. */
public final class NavigationAction {

    public static final String NAVIGATE = "navigate";
    public static final String BACK = "back";
    public static final String FINISH = "finish";
    public static final String REPLACE = "replace";

    public static final String[] ALL = {NAVIGATE, BACK, FINISH, REPLACE};

    private NavigationAction() {
    }

    public static boolean isKnown(String action) {
        if (action == null) {
            return false;
        }
        for (String known : ALL) {
            if (known.equals(action)) {
                return true;
            }
        }
        return false;
    }

    public static String displayName(String action) {
        if (action == null) {
            return NAVIGATE;
        }
        switch (action) {
            case BACK:
                return "Back";
            case FINISH:
                return "Finish";
            case REPLACE:
                return "Replace";
            case NAVIGATE:
            default:
                return "Navigate";
        }
    }
}
