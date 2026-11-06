package com.nobi.appbuilder.navigation;

/**
 * One directed navigation between two screens. Edges derived from a
 * navigate event carry its id in sourceEventId; edges the user draws
 * in the designer have a null sourceEventId. transitionId names a
 * TransitionRegistry transition ("none" by default).
 */
public class NavigationEdge {

    private String id;
    private String fromScreenId;
    private String toScreenId;
    private String trigger;
    private String action = NavigationAction.NAVIGATE;
    private String sourceEventId;
    private String label;
    private String transitionId = "none";
    /** Custom duration in ms (100-1000). Framework anims in preview
     * run at their own fixed timing; the stored value documents the
     * intent and is validated, never silently clamped away. */
    private int durationMs = 300;

    public NavigationEdge() {
    }

    public NavigationEdge(String id, String fromScreenId, String toScreenId,
                          String trigger, String sourceEventId) {
        this.id = id;
        this.fromScreenId = fromScreenId;
        this.toScreenId = toScreenId;
        this.trigger = trigger;
        this.sourceEventId = sourceEventId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromScreenId() {
        return fromScreenId;
    }

    public void setFromScreenId(String fromScreenId) {
        this.fromScreenId = fromScreenId;
    }

    public String getToScreenId() {
        return toScreenId;
    }

    public void setToScreenId(String toScreenId) {
        this.toScreenId = toScreenId;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSourceEventId() {
        return sourceEventId;
    }

    public void setSourceEventId(String sourceEventId) {
        this.sourceEventId = sourceEventId;
    }

    public String getLabel() {
        if (label != null && !label.isEmpty()) {
            return label;
        }
        return trigger == null ? action : trigger;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTransitionId() {
        return transitionId;
    }

    public void setTransitionId(String transitionId) {
        this.transitionId = transitionId;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(int durationMs) {
        this.durationMs = durationMs;
    }

    /** True when this edge came from a navigate event in the IR. */
    public boolean isDerivedFromEvent() {
        return sourceEventId != null && !sourceEventId.isEmpty();
    }
}
