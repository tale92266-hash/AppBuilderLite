package com.nobi.appbuilder.navigation;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.repository.DocumentRepository;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Facade for the navigation designer: load/save/rebuild the graph,
 * add/remove edges, validate, and the preview Back stack.
 */
public class NavigationManager {

    private final DatabaseHelper dbHelper;
    private final NavigationRepository repository;
    private final NavigationHistoryManager history =
            new NavigationHistoryManager();

    public NavigationManager(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.repository = new NavigationRepository(dbHelper);
    }

    public NavigationGraph getGraph(String projectId) {
        return repository.load(projectId);
    }

    public void saveGraph(String projectId, NavigationGraph graph) {
        repository.save(projectId, graph);
    }

    public NavigationGraph rebuild(String projectId) {
        NavigationGraph graph = repository.rebuildKeepingExplicit(projectId);
        repository.save(projectId, graph);
        return graph;
    }

    public List<String> validate(String projectId, NavigationGraph graph) {
        return NavigationValidator.validate(graph, getScreenIds(projectId));
    }

    public List<String> validateWarnings(NavigationGraph graph) {
        return NavigationValidator.validateWarnings(graph);
    }

    public Set<String> getScreenIds(String projectId) {
        Set<String> ids = new HashSet<>();
        IRDocument document = new DocumentRepository(dbHelper)
                .loadDocument(projectId);
        if (document == null) {
            return ids;
        }
        for (ScreenModel screen : document.getScreens()) {
            if (screen.getId() != null) {
                ids.add(screen.getId());
            }
        }
        return ids;
    }

    public List<ScreenModel> getScreens(String projectId) {
        IRDocument document = new DocumentRepository(dbHelper)
                .loadDocument(projectId);
        return document == null ? new ArrayList<>() : document.getScreens();
    }

    /** Adds a designer-drawn edge. Returns null on success. */
    public String addEdge(String projectId, NavigationGraph graph,
                          String fromScreenId, String toScreenId) {
        if (graph.getNode(fromScreenId) == null) {
            return "source screen not found";
        }
        if (graph.getNode(toScreenId) == null) {
            return "destination screen not found";
        }
        if (fromScreenId.equals(toScreenId)) {
            return "a screen cannot navigate to itself";
        }
        NavigationEdge edge = new NavigationEdge(IDGenerator.newId("edge"),
                fromScreenId, toScreenId,
                com.nobi.appbuilder.model.EventModel.EVENT_CLICK, null);
        graph.addEdge(edge);
        repository.save(projectId, graph);
        return null;
    }

    public void removeEdge(String projectId, NavigationGraph graph,
                           String edgeId) {
        graph.removeEdge(edgeId);
        repository.save(projectId, graph);
    }

    public void setTransition(String projectId, NavigationGraph graph,
                              String edgeId, String transitionId) {
        NavigationEdge edge = graph.getEdge(edgeId);
        if (edge == null) {
            return;
        }
        new ScreenTransitionManager().setTransitionForEdge(edge, transitionId);
        repository.save(projectId, graph);
    }

    public NavigationHistoryManager getHistory() {
        return history;
    }
}
