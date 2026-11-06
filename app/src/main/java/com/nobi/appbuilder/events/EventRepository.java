package com.nobi.appbuilder.events;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.project.ProjectSettingsManager;

/**
 * Persists flow graphs per event in project_settings
 * ("flow_graph_<eventId>"). No new SQLite tables: a flow graph is a
 * JSON document, and settings already store per-project JSON.
 */
public class EventRepository {

    public static final String FLOW_GRAPH_KEY_PREFIX = "flow_graph_";

    private final ProjectSettingsManager settings;

    public EventRepository(DatabaseHelper dbHelper) {
        this.settings = new ProjectSettingsManager(dbHelper,
                new ProjectRepository(dbHelper));
    }

    public static String flowGraphKey(String eventId) {
        return FLOW_GRAPH_KEY_PREFIX + eventId;
    }

    public void saveFlowGraph(String projectId, String eventId, String json) {
        if (projectId == null || eventId == null) {
            return;
        }
        settings.setSetting(projectId, flowGraphKey(eventId),
                json == null ? "" : json);
    }

    public String loadFlowGraph(String projectId, String eventId) {
        if (projectId == null || eventId == null) {
            return "";
        }
        String json = settings.getSetting(projectId, flowGraphKey(eventId), "");
        return json == null ? "" : json;
    }

    public boolean hasFlowGraph(String projectId, String eventId) {
        String json = loadFlowGraph(projectId, eventId);
        return json != null && !json.isEmpty();
    }

    public void deleteFlowGraph(String projectId, String eventId) {
        if (projectId == null || eventId == null) {
            return;
        }
        settings.setSetting(projectId, flowGraphKey(eventId), "");
    }
}
