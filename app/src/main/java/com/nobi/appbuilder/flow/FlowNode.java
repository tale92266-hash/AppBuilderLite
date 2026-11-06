package com.nobi.appbuilder.flow;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One node in a workflow graph. Parameters are plain strings:
 * ACTION nodes carry the action id + its params, CONDITION nodes
 * carry operator/left/right, NAVIGATION nodes carry targetScreenId,
 * VARIABLE nodes carry name/value/type.
 */
public class FlowNode {

    private String id;
    private FlowNodeType type = FlowNodeType.ACTION;
    private String label;
    private String actionId;
    private Map<String, String> params = new LinkedHashMap<>();
    private float x;
    private float y;

    public FlowNode() {
    }

    public FlowNode(String id, FlowNodeType type, String label) {
        this.id = id;
        this.type = type;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FlowNodeType getType() {
        return type;
    }

    public void setType(FlowNodeType type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getActionId() {
        return actionId;
    }

    public void setActionId(String actionId) {
        this.actionId = actionId;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params == null ? new LinkedHashMap<>() : params;
    }

    public void setParam(String key, String value) {
        if (key != null && value != null) {
            params.put(key, value);
        }
    }

    public String getParam(String key) {
        return params.get(key);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
