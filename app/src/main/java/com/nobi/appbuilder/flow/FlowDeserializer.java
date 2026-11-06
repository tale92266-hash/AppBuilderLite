package com.nobi.appbuilder.flow;

import com.google.gson.Gson;

/**
 * Flow graphs from JSON. Bad or empty JSON returns null — the caller
 * builds a fresh graph instead of crashing on a corrupt setting.
 */
public final class FlowDeserializer {

    private static final Gson GSON = new Gson();

    private FlowDeserializer() {
    }

    public static FlowGraph fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            FlowGraph graph = GSON.fromJson(json, FlowGraph.class);
            if (graph == null || graph.getNodes() == null
                    || graph.getConnections() == null) {
                return null;
            }
            return graph;
        } catch (Exception e) {
            return null;
        }
    }
}
