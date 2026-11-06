package com.nobi.appbuilder.navigation;

import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Stable routes for screens. A route is the screen id itself — ids
 * never change when a screen is renamed, so a saved route never goes
 * stale. Display names are only ever shown, never used as keys.
 */
public final class ScreenRouteManager {

    private ScreenRouteManager() {
    }

    public static String routeFor(String screenId) {
        return screenId == null ? "" : screenId;
    }

    public static ScreenModel screenForRoute(IRDocument document,
                                             String route) {
        if (document == null || route == null) {
            return null;
        }
        return document.getScreen(route);
    }

    public static List<String> listRoutes(IRDocument document) {
        List<String> routes = new ArrayList<>();
        if (document == null) {
            return routes;
        }
        for (ScreenModel screen : document.getScreens()) {
            routes.add(routeFor(screen.getId()));
        }
        return routes;
    }

    public static String displayNameFor(IRDocument document, String route) {
        ScreenModel screen = screenForRoute(document, route);
        return screen == null ? route : screen.getName();
    }
}
