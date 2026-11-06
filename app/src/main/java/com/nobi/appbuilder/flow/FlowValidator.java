package com.nobi.appbuilder.flow;

import com.nobi.appbuilder.actions.ActionRegistry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates a flow graph. Errors block execution — an invalid graph
 * is never silently executed. Warnings (unreachable nodes, cycles)
 * are reported separately and do not block.
 */
public final class FlowValidator {

    private FlowValidator() {
    }

    /** Errors: execution is refused while any of these exist. */
    public static List<String> validate(FlowGraph graph) {
        List<String> errors = new ArrayList<>();
        if (graph == null) {
            errors.add("flow graph is missing");
            return errors;
        }
        if (graph.getNodes().isEmpty()) {
            errors.add("flow graph has no nodes");
            return errors;
        }
        Set<String> ids = new HashSet<>();
        int starts = 0;
        for (FlowNode node : graph.getNodes()) {
            if (node.getId() == null || node.getId().isEmpty()) {
                errors.add("a node has no id");
                continue;
            }
            if (!ids.add(node.getId())) {
                errors.add("duplicate node id '" + node.getId() + "'");
            }
            if (node.getType() == FlowNodeType.START) {
                starts++;
            }
            if (node.getType() == FlowNodeType.ACTION) {
                if (node.getActionId() == null
                        || !ActionRegistry.contains(node.getActionId())) {
                    errors.add("node '" + node.getId()
                            + "' has unknown action '" + node.getActionId()
                            + "'");
                }
            }
            if (node.getType() == FlowNodeType.NAVIGATION) {
                String target = node.getParam("targetScreenId");
                if (target == null || target.isEmpty()) {
                    errors.add("navigation node '" + node.getId()
                            + "' has no target screen");
                }
            }
            if (node.getType() == FlowNodeType.CONDITION) {
                if (node.getParam("operator") == null
                        || node.getParam("left") == null) {
                    errors.add("condition node '" + node.getId()
                            + "' needs operator and left value");
                }
                if (graph.nextNode(node.getId(), FlowConnection.PORT_TRUE)
                        == null) {
                    errors.add("condition node '" + node.getId()
                            + "' has no 'true' connection");
                }
                if (graph.nextNode(node.getId(), FlowConnection.PORT_FALSE)
                        == null) {
                    errors.add("condition node '" + node.getId()
                            + "' has no 'false' connection");
                }
            }
        }
        if (starts == 0) {
            errors.add("flow graph has no Start node");
        } else if (starts > 1) {
            errors.add("flow graph has more than one Start node");
        }
        if (graph.getEndNodes().isEmpty()) {
            errors.add("flow graph has no End node");
        }
        for (FlowConnection connection : graph.getConnections()) {
            if (graph.getNode(connection.getFromNodeId()) == null) {
                errors.add("connection '" + connection.getId()
                        + "' starts at missing node '"
                        + connection.getFromNodeId() + "'");
            }
            if (graph.getNode(connection.getToNodeId()) == null) {
                errors.add("connection '" + connection.getId()
                        + "' ends at missing node '"
                        + connection.getToNodeId() + "'");
            }
        }
        return errors;
    }

    /** Warnings: reported, but they do not block execution. */
    public static List<String> validateWarnings(FlowGraph graph) {
        List<String> warnings = new ArrayList<>();
        if (graph == null || graph.getStartNode() == null) {
            return warnings;
        }
        Set<String> reachable = new HashSet<>();
        collectReachable(graph, graph.getStartNode().getId(), reachable,
                new HashSet<>(), warnings);
        for (FlowNode node : graph.getNodes()) {
            if (!reachable.contains(node.getId())) {
                warnings.add("node '" + node.getId()
                        + "' (" + node.getType().displayName()
                        + ") is unreachable from Start");
            }
        }
        return warnings;
    }

    private static void collectReachable(FlowGraph graph, String nodeId,
                                         Set<String> reachable,
                                         Set<String> visiting,
                                         List<String> warnings) {
        if (nodeId == null || reachable.contains(nodeId)) {
            if (nodeId != null && visiting.contains(nodeId)) {
                warnings.add("cycle detected through node '" + nodeId
                        + "' — it will warn, not loop forever");
            }
            return;
        }
        visiting.add(nodeId);
        reachable.add(nodeId);
        for (FlowConnection connection : graph.outgoing(nodeId)) {
            collectReachable(graph, connection.getToNodeId(), reachable,
                    visiting, warnings);
        }
        visiting.remove(nodeId);
    }
}
