package com.nobi.appbuilder.navigation;

import com.google.gson.Gson;

/**
 * Navigation graphs from JSON. Bad or empty JSON returns null — the
 * caller rebuilds from the document instead of crashing.
 */
public final class NavigationDeserializer {

    private static final Gson GSON = new Gson();

    private NavigationDeserializer() {
    }

    public static NavigationGraph fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            NavigationGraph graph = GSON.fromJson(json, NavigationGraph.class);
            if (graph == null || graph.getNodes() == null
                    || graph.getEdges() == null) {
                return null;
            }
            return graph;
        } catch (Exception e) {
            return null;
        }
    }
}
