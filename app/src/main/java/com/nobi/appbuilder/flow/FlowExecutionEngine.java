package com.nobi.appbuilder.flow;

import com.nobi.appbuilder.actions.ActionExecutor;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.builtins.ConditionalAction;
import com.nobi.appbuilder.events.EventExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a flow graph step by step. Invalid graphs are refused, not
 * executed. Limits: 100 steps per run, 10 visits per node — a cycle
 * can warn in the validator but can never loop forever here. Action
 * failures stop the run with the reason; conditions branch on typed
 * comparisons only, never on evaluated expressions.
 */
public class FlowExecutionEngine {

    private final ActionExecutor actionExecutor = new ActionExecutor();

    /** Runs the graph to completion (or to the first failure). */
    public FlowExecutionResult execute(FlowGraph graph,
                                       FlowExecutionContext context,
                                       EventExecutionContext eventContext) {
        List<String> errors = FlowValidator.validate(graph);
        List<String> executed = new ArrayList<>();
        if (!errors.isEmpty()) {
            context.setError(errors.get(0));
            context.log("Flow refused: " + errors.get(0));
            return FlowExecutionResult.fromContext(context, executed, false);
        }
        FlowNode start = graph.getStartNode();
        context.setCurrentNodeId(start.getId());
        while (!context.isFinished() && context.getError() == null) {
            if (context.getSteps() >= FlowExecutionContext.MAX_STEPS) {
                context.setError("flow stopped: step limit ("
                        + FlowExecutionContext.MAX_STEPS + ") reached");
                break;
            }
            if (!stepOnce(graph, context, eventContext, executed)) {
                break;
            }
        }
        boolean ok = context.getError() == null && context.isFinished();
        return FlowExecutionResult.fromContext(context, executed, ok);
    }

    /**
     * Runs exactly one node (debug stepping). Returns the result so
     * far; the caller keeps the same context and calls again.
     */
    public FlowExecutionResult executeStep(FlowGraph graph,
                                           FlowExecutionContext context,
                                           EventExecutionContext eventContext) {
        List<String> errors = FlowValidator.validate(graph);
        List<String> executed = new ArrayList<>();
        if (!errors.isEmpty()) {
            context.setError(errors.get(0));
            return FlowExecutionResult.fromContext(context, executed, false);
        }
        if (context.getCurrentNodeId() == null) {
            FlowNode start = graph.getStartNode();
            context.setCurrentNodeId(start.getId());
        }
        if (!context.isFinished() && context.getError() == null) {
            stepOnce(graph, context, eventContext, executed);
        }
        boolean ok = context.getError() == null;
        return FlowExecutionResult.fromContext(context, executed, ok);
    }

    /** Executes the current node and advances. False = run stops. */
    private boolean stepOnce(FlowGraph graph, FlowExecutionContext context,
                             EventExecutionContext eventContext,
                             List<String> executed) {
        FlowNode node = graph.getNode(context.getCurrentNodeId());
        if (node == null) {
            context.setError("current node '"
                    + context.getCurrentNodeId() + "' not found");
            return false;
        }
        if (!context.recordVisit(node.getId())) {
            context.setError("possible infinite loop at node '"
                    + node.getId() + "' (visited more than "
                    + FlowExecutionContext.MAX_VISITS_PER_NODE + " times)");
            return false;
        }
        context.incrementSteps();
        executed.add(node.getId());
        context.log("Step: " + node.getType().displayName()
                + " '" + node.getLabel() + "'");
        switch (node.getType()) {
            case START:
            case EVENT:
                return advance(graph, context,
                        graph.nextNode(node.getId(), FlowConnection.PORT_NEXT));
            case END:
                context.setFinished(true);
                context.log("Flow finished");
                return false;
            case ACTION: {
                ActionResult result = actionExecutor.execute(
                        node.getActionId(), node.getParams(), eventContext);
                if (result == null || !result.isSuccess()) {
                    context.setError(result == null
                            ? "action failed"
                            : result.getMessage());
                    return false;
                }
                if (result.getMessage() != null) {
                    context.log(result.getMessage());
                }
                return advance(graph, context,
                        graph.nextNode(node.getId(), FlowConnection.PORT_NEXT));
            }
            case CONDITION: {
                String left = ConditionalAction.resolve(
                        node.getParam("left"), eventContext);
                Boolean outcome = ConditionalAction.evaluate(
                        node.getParam("operator"), left,
                        node.getParam("right"));
                if (outcome == null) {
                    context.setError("unknown operator '"
                            + node.getParam("operator") + "'");
                    return false;
                }
                context.recordCondition(node.getId(), outcome);
                context.log("Condition '" + node.getLabel() + "' = " + outcome);
                return advance(graph, context, graph.nextNode(node.getId(),
                        outcome ? FlowConnection.PORT_TRUE
                                : FlowConnection.PORT_FALSE));
            }
            case NAVIGATION: {
                String target = node.getParam("targetScreenId");
                context.recordNavigation(target);
                if (eventContext != null) {
                    eventContext.recordNavigation(target);
                }
                context.log("Navigate to " + target);
                return advance(graph, context,
                        graph.nextNode(node.getId(), FlowConnection.PORT_NEXT));
            }
            case VARIABLE: {
                FlowVariableManager variables =
                        new FlowVariableManager(context);
                String problem = variables.set(node.getParam("name"),
                        node.getParam("value"), node.getParam("type"));
                if (problem != null) {
                    context.setError(problem);
                    return false;
                }
                context.log("Set variable " + node.getParam("name")
                        + " = " + node.getParam("value"));
                return advance(graph, context,
                        graph.nextNode(node.getId(), FlowConnection.PORT_NEXT));
            }
            default:
                context.setError("unknown node type");
                return false;
        }
    }

    private boolean advance(FlowGraph graph, FlowExecutionContext context,
                            FlowNode next) {
        if (next == null) {
            context.setError("flow has no next node — "
                    + "connect every node or end at an End node");
            return false;
        }
        context.setCurrentNodeId(next.getId());
        return true;
    }
}
