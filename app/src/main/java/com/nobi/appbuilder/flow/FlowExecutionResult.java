package com.nobi.appbuilder.flow;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The outcome of a flow run (or of one debug step): whether it
 * finished, where it stopped, what ran, what the variables hold, and
 * any error that stopped it.
 */
public class FlowExecutionResult {

    private boolean success;
    private boolean finished;
    private boolean paused;
    private String error;
    private String currentNodeId;
    private final List<String> executedNodeIds = new ArrayList<>();
    private final List<String> log = new ArrayList<>();
    private final List<String> navigationEvents = new ArrayList<>();
    private final Map<String, Boolean> conditionResults = new LinkedHashMap<>();
    private final Map<String, String> variables = new LinkedHashMap<>();

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCurrentNodeId() {
        return currentNodeId;
    }

    public void setCurrentNodeId(String currentNodeId) {
        this.currentNodeId = currentNodeId;
    }

    public List<String> getExecutedNodeIds() {
        return executedNodeIds;
    }

    public List<String> getLog() {
        return log;
    }

    public List<String> getNavigationEvents() {
        return navigationEvents;
    }

    public Map<String, Boolean> getConditionResults() {
        return conditionResults;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public static FlowExecutionResult fromContext(FlowExecutionContext context,
                                                  List<String> executed,
                                                  boolean success) {
        FlowExecutionResult result = new FlowExecutionResult();
        result.setSuccess(success);
        result.setFinished(context.isFinished());
        result.setPaused(context.isPaused());
        result.setError(context.getError());
        result.setCurrentNodeId(context.getCurrentNodeId());
        result.getExecutedNodeIds().addAll(executed);
        result.getLog().addAll(context.getLog());
        result.getNavigationEvents().addAll(context.getNavigationEvents());
        result.getConditionResults().putAll(context.getConditionResults());
        result.getVariables().putAll(context.getVariables());
        return result;
    }
}
