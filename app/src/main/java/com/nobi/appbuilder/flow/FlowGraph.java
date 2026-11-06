package com.nobi.appbuilder.flow;

import java.util.ArrayList;
import java.util.List;

/**
 * One workflow graph, owned by one event (graph id = event id).
 * Nodes and connections are plain data — the engine walks them, the
 * canvas draws them, JSON stores them.
 */
public class FlowGraph {

    private String id;
    private String name;
    private List<FlowNode> nodes = new ArrayList<>();
    private List<FlowConnection> connections = new ArrayList<>();

    public FlowGraph() {
    }

    public FlowGraph(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<FlowNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<FlowNode> nodes) {
        this.nodes = nodes == null ? new ArrayList<>() : nodes;
    }

    public List<FlowConnection> getConnections() {
        return connections;
    }

    public void setConnections(List<FlowConnection> connections) {
        this.connections = connections == null
                ? new ArrayList<>() : connections;
    }

    public FlowNode getNode(String nodeId) {
        if (nodeId == null) {
            return null;
        }
        for (FlowNode node : nodes) {
            if (nodeId.equals(node.getId())) {
                return node;
            }
        }
        return null;
    }

    public FlowNode getStartNode() {
        for (FlowNode node : nodes) {
            if (node.getType() == FlowNodeType.START) {
                return node;
            }
        }
        return null;
    }

    public List<FlowNode> getEndNodes() {
        List<FlowNode> out = new ArrayList<>();
        for (FlowNode node : nodes) {
            if (node.getType() == FlowNodeType.END) {
                out.add(node);
            }
        }
        return out;
    }

    /** The node reached from (nodeId, port), or null. */
    public FlowNode nextNode(String nodeId, String port) {
        for (FlowConnection connection : connections) {
            if (nodeId != null && nodeId.equals(connection.getFromNodeId())
                    && port != null && port.equals(connection.getFromPort())) {
                return getNode(connection.getToNodeId());
            }
        }
        return null;
    }

    public List<FlowConnection> outgoing(String nodeId) {
        List<FlowConnection> out = new ArrayList<>();
        for (FlowConnection connection : connections) {
            if (nodeId != null
                    && nodeId.equals(connection.getFromNodeId())) {
                out.add(connection);
            }
        }
        return out;
    }

    public void addNode(FlowNode node) {
        if (node != null && getNode(node.getId()) == null) {
            nodes.add(node);
        }
    }

    public void removeNode(String nodeId) {
        nodes.removeIf(node -> nodeId != null
                && nodeId.equals(node.getId()));
        connections.removeIf(connection ->
                nodeId.equals(connection.getFromNodeId())
                        || nodeId.equals(connection.getToNodeId()));
    }

    public void addConnection(FlowConnection connection) {
        if (connection != null) {
            connections.add(connection);
        }
    }

    public void removeConnection(String connectionId) {
        connections.removeIf(connection -> connectionId != null
                && connectionId.equals(connection.getId()));
    }
}
