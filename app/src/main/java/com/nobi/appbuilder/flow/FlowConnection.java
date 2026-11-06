package com.nobi.appbuilder.flow;

/**
 * One directed edge between flow nodes. Ports: "next" for linear
 * nodes, "true"/"false" for CONDITION nodes.
 */
public class FlowConnection {

    public static final String PORT_NEXT = "next";
    public static final String PORT_TRUE = "true";
    public static final String PORT_FALSE = "false";

    private String id;
    private String fromNodeId;
    private String fromPort = PORT_NEXT;
    private String toNodeId;

    public FlowConnection() {
    }

    public FlowConnection(String id, String fromNodeId, String fromPort,
                          String toNodeId) {
        this.id = id;
        this.fromNodeId = fromNodeId;
        this.fromPort = fromPort;
        this.toNodeId = toNodeId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromNodeId() {
        return fromNodeId;
    }

    public void setFromNodeId(String fromNodeId) {
        this.fromNodeId = fromNodeId;
    }

    public String getFromPort() {
        return fromPort;
    }

    public void setFromPort(String fromPort) {
        this.fromPort = fromPort;
    }

    public String getToNodeId() {
        return toNodeId;
    }

    public void setToNodeId(String toNodeId) {
        this.toNodeId = toNodeId;
    }
}
