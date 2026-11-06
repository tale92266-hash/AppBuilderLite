package com.nobi.appbuilder.navigation;

import java.util.ArrayList;
import java.util.List;

/**
 * The navigation graph of one project: one node per screen, one edge
 * per navigation. Nodes are keyed by stable screen ids.
 */
public class NavigationGraph {

    private String startingScreenId;
    private List<NavigationNode> nodes = new ArrayList<>();
    private List<NavigationEdge> edges = new ArrayList<>();

    public NavigationGraph() {
    }

    public String getStartingScreenId() {
        return startingScreenId;
    }

    public void setStartingScreenId(String startingScreenId) {
        this.startingScreenId = startingScreenId;
    }

    public List<NavigationNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<NavigationNode> nodes) {
        this.nodes = nodes == null ? new ArrayList<>() : nodes;
    }

    public List<NavigationEdge> getEdges() {
        return edges;
    }

    public void setEdges(List<NavigationEdge> edges) {
        this.edges = edges == null ? new ArrayList<>() : edges;
    }

    public NavigationNode getNode(String screenId) {
        if (screenId == null) {
            return null;
        }
        for (NavigationNode node : nodes) {
            if (screenId.equals(node.getScreenId())) {
                return node;
            }
        }
        return null;
    }

    public NavigationEdge getEdge(String edgeId) {
        if (edgeId == null) {
            return null;
        }
        for (NavigationEdge edge : edges) {
            if (edgeId.equals(edge.getId())) {
                return edge;
            }
        }
        return null;
    }

    public List<NavigationEdge> outgoingEdges(String screenId) {
        List<NavigationEdge> out = new ArrayList<>();
        for (NavigationEdge edge : edges) {
            if (screenId != null && screenId.equals(edge.getFromScreenId())) {
                out.add(edge);
            }
        }
        return out;
    }

    public List<NavigationEdge> incomingEdges(String screenId) {
        List<NavigationEdge> out = new ArrayList<>();
        for (NavigationEdge edge : edges) {
            if (screenId != null && screenId.equals(edge.getToScreenId())) {
                out.add(edge);
            }
        }
        return out;
    }

    public void addNode(NavigationNode node) {
        if (node != null && getNode(node.getScreenId()) == null) {
            nodes.add(node);
        }
    }

    public void addEdge(NavigationEdge edge) {
        if (edge != null && getEdge(edge.getId()) == null) {
            edges.add(edge);
        }
    }

    public void removeEdge(String edgeId) {
        edges.removeIf(edge -> edgeId != null
                && edgeId.equals(edge.getId()));
    }

    public void removeNode(String screenId) {
        nodes.removeIf(node -> screenId != null
                && screenId.equals(node.getScreenId()));
        edges.removeIf(edge -> screenId.equals(edge.getFromScreenId())
                || screenId.equals(edge.getToScreenId()));
    }
}
