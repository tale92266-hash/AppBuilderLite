package com.nobi.appbuilder.navigation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates a navigation graph. Errors: edges pointing at missing
 * screens, unknown actions, a missing start screen. Warnings only:
 * unreachable screens and cycles — a cycle is not automatically
 * invalid (Back navigation is a cycle by design).
 */
public final class NavigationValidator {

    private NavigationValidator() {
    }

    public static List<String> validate(NavigationGraph graph,
                                        Set<String> existingScreenIds) {
        List<String> errors = new ArrayList<>();
        if (graph == null) {
            errors.add("navigation graph is missing");
            return errors;
        }
        if (graph.getStartingScreenId() == null
                || graph.getStartingScreenId().isEmpty()) {
            errors.add("no starting screen set");
        } else if (graph.getNode(graph.getStartingScreenId()) == null) {
            errors.add("starting screen '"
                    + graph.getStartingScreenId() + "' does not exist");
        }
        Set<String> edgeIds = new HashSet<>();
        for (NavigationEdge edge : graph.getEdges()) {
            if (edge.getId() == null || edge.getId().isEmpty()) {
                errors.add("an edge has no id");
                continue;
            }
            if (!edgeIds.add(edge.getId())) {
                errors.add("duplicate edge id '" + edge.getId() + "'");
            }
            if (graph.getNode(edge.getFromScreenId()) == null) {
                errors.add("edge '" + edge.getId()
                        + "' starts at missing screen '"
                        + edge.getFromScreenId() + "'");
            }
            if (graph.getNode(edge.getToScreenId()) == null) {
                errors.add("edge '" + edge.getId()
                        + "' points at missing screen '"
                        + edge.getToScreenId() + "'");
            }
            if (existingScreenIds != null
                    && edge.getToScreenId() != null
                    && !existingScreenIds.contains(edge.getToScreenId())) {
                errors.add("edge '" + edge.getId()
                        + "' destination '" + edge.getToScreenId()
                        + "' is not a screen in this project");
            }
            if (!NavigationAction.isKnown(edge.getAction())) {
                errors.add("edge '" + edge.getId()
                        + "' has unknown action '" + edge.getAction() + "'");
            }
        }
        return errors;
    }

    public static List<String> validateWarnings(NavigationGraph graph) {
        List<String> warnings = new ArrayList<>();
        if (graph == null || graph.getStartingScreenId() == null
                || graph.getNode(graph.getStartingScreenId()) == null) {
            return warnings;
        }
        Set<String> reachable = new HashSet<>();
        collectReachable(graph, graph.getStartingScreenId(), reachable,
                new HashSet<>(), warnings);
        for (NavigationNode node : graph.getNodes()) {
            if (!reachable.contains(node.getScreenId())) {
                warnings.add("screen '" + node.getName()
                        + "' is unreachable from the start screen");
            }
        }
        return warnings;
    }

    private static void collectReachable(NavigationGraph graph,
                                         String screenId,
                                         Set<String> reachable,
                                         Set<String> visiting,
                                         List<String> warnings) {
        if (screenId == null || reachable.contains(screenId)) {
            if (screenId != null && visiting.contains(screenId)) {
                warnings.add("navigation cycle through '"
                        + screenId + "' — allowed, but check the Back path");
            }
            return;
        }
        visiting.add(screenId);
        reachable.add(screenId);
        for (NavigationEdge edge : graph.outgoingEdges(screenId)) {
            collectReachable(graph, edge.getToScreenId(), reachable,
                    visiting, warnings);
        }
        visiting.remove(screenId);
    }
}
