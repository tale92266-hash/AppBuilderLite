package com.nobi.appbuilder.flow;

import com.google.gson.Gson;

/** Flow graphs to/from JSON. */
public final class FlowSerializer {

    private static final Gson GSON = new Gson();

    private FlowSerializer() {
    }

    public static String toJson(FlowGraph graph) {
        if (graph == null) {
            return "";
        }
        return GSON.toJson(graph);
    }
}
