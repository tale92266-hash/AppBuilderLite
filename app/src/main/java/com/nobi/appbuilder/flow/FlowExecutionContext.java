package com.nobi.appbuilder.flow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mutable state of one flow run: typed variables, the current node,
 * the step log, navigation events and the pause/finish flags the
 * debugger drives. Step and visit limits live here too.
 */
public class FlowExecutionContext {

    public static final int MAX_STEPS = 100;
    public static final int MAX_VISITS_PER_NODE = 10;

    private final Map<String, String> variables = new LinkedHashMap<>();
    private String currentNodeId;
    private int steps;
    private final Map<String, Integer> visits = new LinkedHashMap<>();
    private final List<String> log = new ArrayList<>();
    private final List<String> navigationEvents = new ArrayList<>();
    private final Map<String, Boolean> conditionResults = new LinkedHashMap<>();
    private boolean paused;
    private boolean finished;
    private String error;

    public Map<String, String> getVariables() {
        return variables;
    }

    public String getVariable(String name) {
        return variables.get(name);
    }

    public void setVariable(String name, String value) {
        if (name != null && value != null) {
            variables.put(name, value);
        }
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public int getSteps() {
        return steps;
    }

    public void incrementSteps() {
        steps++;
    }

    /** Counts a visit; returns false when the node was visited too often. */
    public boolean recordVisit(String nodeId) {
        if (nodeId == null) {
            return true;
        }
        int count = visits.getOrDefault(nodeId, 0) + 1;
        visits.put(nodeId, count);
        return count <= MAX_VISITS_PER_NODE;
    }

    public void log(String entry) {
        if (entry != null) {
            log.add(entry);
        }
    }

    public List<String> getLog() {
        return log;
    }

    public void recordNavigation(String targetScreenId) {
        if (targetScreenId != null) {
            navigationEvents.add(targetScreenId);
        }
    }

    public List<String> getNavigationEvents() {
        return navigationEvents;
    }

    public void recordCondition(String nodeId, boolean result) {
        if (nodeId != null) {
            conditionResults.put(nodeId, result);
        }
    }

    public Map<String, Boolean> getConditionResults() {
        return conditionResults;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
