package com.nobi.appbuilder.navigation;

import com.google.gson.Gson;

/** Navigation graphs to JSON. */
public final class NavigationSerializer {

    private static final Gson GSON = new Gson();

    private NavigationSerializer() {
    }

    public static String toJson(NavigationGraph graph) {
        if (graph == null) {
            return "";
        }
        return GSON.toJson(graph);
    }
}
