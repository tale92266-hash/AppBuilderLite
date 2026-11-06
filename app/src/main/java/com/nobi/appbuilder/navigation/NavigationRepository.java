package com.nobi.appbuilder.navigation;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.events.EventDefinition;
import com.nobi.appbuilder.events.EventManager;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.project.ProjectSettingsManager;
import com.nobi.appbuilder.repository.DocumentRepository;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.HashSet;
import java.util.Set;

/**
 * Loads/saves the navigation graph as JSON in project_settings
 * (key "navigation_graph_json") — no new SQLite tables. Rebuilding
 * from the document keeps stored explicit edges and re-derives edges
 * from navigate events, so the graph never goes stale when events
 * change.
 */
public class NavigationRepository {

    public static final String GRAPH_KEY = "navigation_graph_json";

    private final DatabaseHelper dbHelper;
    private final ProjectSettingsManager settings;
    private final DocumentRepository documents;

    public NavigationRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.settings = new ProjectSettingsManager(dbHelper,
                new ProjectRepository(dbHelper));
        this.documents = new DocumentRepository(dbHelper);
    }

    public NavigationGraph load(String projectId) {
        NavigationGraph graph = NavigationDeserializer.fromJson(
                settings.getSetting(projectId, GRAPH_KEY, ""));
        if (graph != null) {
            return graph;
        }
        return buildFromDocument(projectId);
    }

    public void save(String projectId, NavigationGraph graph) {
        settings.setSetting(projectId, GRAPH_KEY,
                NavigationSerializer.toJson(graph));
    }

    /** Builds a fresh graph from screens + navigate events. */
    public NavigationGraph buildFromDocument(String projectId) {
        NavigationGraph graph = new NavigationGraph();
        IRDocument document = documents.loadDocument(projectId);
        if (document == null) {
            return graph;
        }
        int index = 0;
        for (ScreenModel screen : document.getScreens()) {
            NavigationNode node = new NavigationNode(screen.getId(),
                    screen.getName(), screen.isLauncher(),
                    40 + index * 240, 40);
            graph.addNode(node);
            if (screen.isLauncher()) {
                graph.setStartingScreenId(screen.getId());
            }
            index++;
        }
        if (graph.getStartingScreenId() == null
                && !document.getScreens().isEmpty()) {
            graph.setStartingScreenId(
                    document.getScreens().get(0).getId());
        }
        EventManager events = new EventManager(dbHelper);
        for (ScreenModel screen : document.getScreens()) {
            for (EventDefinition def : events.getScreenEvents(projectId,
                    screen.getId())) {
                if (!EventModel.ACTION_NAVIGATE.equals(def.getAction())) {
                    continue;
                }
                if (def.getTargetScreenId() == null
                        || graph.getNode(def.getTargetScreenId()) == null) {
                    continue;
                }
                NavigationEdge edge = new NavigationEdge(
                        IDGenerator.newId("edge"), screen.getId(),
                        def.getTargetScreenId(), def.getTrigger(),
                        def.getId());
                graph.addEdge(edge);
            }
        }
        return graph;
    }

    /**
     * Rebuilds while keeping the user's explicit edges and stored
     * transitions: stored edges whose screens still exist survive,
     * event-derived edges are re-added when missing.
     */
    public NavigationGraph rebuildKeepingExplicit(String projectId) {
        NavigationGraph stored = NavigationDeserializer.fromJson(
                settings.getSetting(projectId, GRAPH_KEY, ""));
        NavigationGraph fresh = buildFromDocument(projectId);
        if (stored == null) {
            return fresh;
        }
        for (NavigationEdge edge : stored.getEdges()) {
            if (fresh.getNode(edge.getFromScreenId()) == null
                    || fresh.getNode(edge.getToScreenId()) == null) {
                continue;
            }
            boolean alreadyThere = false;
            for (NavigationEdge existing : fresh.getEdges()) {
                if (existing.isDerivedFromEvent()
                        == edge.isDerivedFromEvent()
                        && existing.getFromScreenId()
                        .equals(edge.getFromScreenId())
                        && existing.getToScreenId()
                        .equals(edge.getToScreenId())) {
                    alreadyThere = true;
                    if (existing.isDerivedFromEvent()) {
                        existing.setTransitionId(edge.getTransitionId());
                    }
                    break;
                }
            }
            if (!alreadyThere) {
                fresh.addEdge(edge);
            }
        }
        if (stored.getStartingScreenId() != null
                && fresh.getNode(stored.getStartingScreenId()) != null) {
            fresh.setStartingScreenId(stored.getStartingScreenId());
        }
        return fresh;
    }

    /** All component ids that carry at least one event, per screen. */
    public Set<String> eventComponentIds(String projectId, String screenId) {
        Set<String> ids = new HashSet<>();
        IRDocument document = documents.loadDocument(projectId);
        if (document == null) {
            return ids;
        }
        ScreenModel screen = document.getScreen(screenId);
        if (screen == null || screen.getRootComponent() == null) {
            return ids;
        }
        collectEventComponents(screen.getRootComponent(), ids);
        return ids;
    }

    private void collectEventComponents(ComponentModel model, Set<String> ids) {
        if (model == null) {
            return;
        }
        if (model.getEvents() != null && !model.getEvents().isEmpty()) {
            ids.add(model.getId());
        }
        for (ComponentModel child : model.getChildren()) {
            collectEventComponents(child, ids);
        }
    }
}
