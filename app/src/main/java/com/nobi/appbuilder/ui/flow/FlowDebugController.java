package com.nobi.appbuilder.ui.flow;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.flow.FlowEditorManager;
import com.nobi.appbuilder.flow.FlowExecutionContext;
import com.nobi.appbuilder.flow.FlowExecutionResult;
import com.nobi.appbuilder.flow.FlowGraph;
import com.nobi.appbuilder.flow.FlowNode;

import java.util.Map;

/**
 * The flow debugger: a dialog showing the current node, executed
 * actions, variable values, condition results, navigation events and
 * validation errors, with Start / Step / Run to end / Reset controls.
 * Stepping reuses the same execution context; Reset starts over.
 */
public class FlowDebugController {

    private final Context context;
    private final String projectId;
    private final String screenId;
    private final String componentId;
    private final FlowGraph graph;
    private final FlowEditorManager manager;
    private FlowExecutionContext executionContext;
    private EventExecutionContext eventContext;
    private TextView statusView;
    private TextView logView;

    public FlowDebugController(Context context, String projectId,
                               String screenId, String componentId,
                               FlowGraph graph, FlowEditorManager manager) {
        this.context = context;
        this.projectId = projectId;
        this.screenId = screenId;
        this.componentId = componentId;
        this.graph = graph;
        this.manager = manager;
        resetContexts();
    }

    public void show() {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * context.getResources()
                .getDisplayMetrics().density);
        root.setPadding(padding, padding, padding, padding);

        statusView = new TextView(context);
        statusView.setTextSize(14);
        root.addView(statusView);

        ScrollView scroll = new ScrollView(context);
        logView = new TextView(context);
        logView.setTextSize(13);
        scroll.addView(logView);
        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        root.addView(scroll, scrollParams);

        LinearLayout buttons = new LinearLayout(context);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        Button step = new Button(context);
        step.setText("Step");
        step.setOnClickListener(v -> doStep());
        Button run = new Button(context);
        run.setText("Run");
        run.setOnClickListener(v -> doRun());
        Button reset = new Button(context);
        reset.setText("Reset");
        reset.setOnClickListener(v -> {
            resetContexts();
            refresh();
        });
        buttons.addView(step);
        buttons.addView(run);
        buttons.addView(reset);
        root.addView(buttons);

        new AlertDialog.Builder(context)
                .setTitle("Flow debugger")
                .setView(root)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        refresh();
    }

    private void resetContexts() {
        executionContext = new FlowExecutionContext();
        eventContext = new EventExecutionContext(projectId, screenId,
                componentId);
        eventContext.setFlowContext(executionContext);
        FlowNode start = graph.getStartNode();
        if (start != null) {
            executionContext.setCurrentNodeId(start.getId());
        }
    }

    private void doStep() {
        if (executionContext.isFinished()
                || executionContext.getError() != null) {
            return;
        }
        manager.step(graph, executionContext, eventContext);
        refresh();
    }

    private void doRun() {
        if (executionContext.isFinished()
                || executionContext.getError() != null) {
            return;
        }
        // Run to the end from the current node, in steps.
        while (!executionContext.isFinished()
                && executionContext.getError() == null
                && executionContext.getSteps()
                < FlowExecutionContext.MAX_STEPS) {
            manager.step(graph, executionContext, eventContext);
        }
        refresh();
    }

    private void refresh() {
        if (statusView == null || logView == null) {
            return;
        }
        StringBuilder status = new StringBuilder();
        FlowNode current = graph.getNode(executionContext.getCurrentNodeId());
        status.append("Current: ")
                .append(current == null ? "—" : current.getLabel())
                .append("\nSteps: ").append(executionContext.getSteps());
        if (executionContext.isFinished()) {
            status.append("\nFinished");
        }
        if (executionContext.getError() != null) {
            status.append("\nError: ").append(executionContext.getError());
        }
        if (!executionContext.getVariables().isEmpty()) {
            status.append("\nVariables:");
            for (Map.Entry<String, String> entry
                    : executionContext.getVariables().entrySet()) {
                status.append("\n  ").append(entry.getKey())
                        .append(" = ").append(entry.getValue());
            }
        }
        if (!executionContext.getConditionResults().isEmpty()) {
            status.append("\nConditions:");
            for (Map.Entry<String, Boolean> entry
                    : executionContext.getConditionResults().entrySet()) {
                FlowNode node = graph.getNode(entry.getKey());
                status.append("\n  ")
                        .append(node == null ? entry.getKey()
                                : node.getLabel())
                        .append(" = ").append(entry.getValue());
            }
        }
        if (!executionContext.getNavigationEvents().isEmpty()) {
            status.append("\nNavigation: ")
                    .append(String.join(", ",
                            executionContext.getNavigationEvents()));
        }
        statusView.setText(status.toString());
        StringBuilder log = new StringBuilder();
        for (String line : executionContext.getLog()) {
            log.append(line).append("\n");
        }
        FlowExecutionResult probe = FlowExecutionResult.fromContext(
                executionContext,
                new java.util.ArrayList<>(), true);
        if (probe.getError() != null) {
            log.append("Error: ").append(probe.getError()).append("\n");
        }
        logView.setText(log.toString());
    }
}
