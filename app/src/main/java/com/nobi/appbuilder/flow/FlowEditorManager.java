package com.nobi.appbuilder.flow;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.events.EventRepository;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.List;

/**
 * Facade for the workflow editor: load/save one flow graph per event,
 * validate it, and run it. A missing or corrupt graph opens as a
 * fresh Start → End graph, never as a crash.
 */
public class FlowEditorManager {

    private final EventRepository repository;
    private final FlowExecutionEngine engine = new FlowExecutionEngine();

    public FlowEditorManager(DatabaseHelper dbHelper) {
        this.repository = new EventRepository(dbHelper);
    }

    public FlowGraph loadGraph(String projectId, String eventId) {
        FlowGraph graph = FlowDeserializer.fromJson(
                repository.loadFlowGraph(projectId, eventId));
        if (graph != null) {
            return graph;
        }
        return freshGraph(eventId);
    }

    public FlowGraph freshGraph(String eventId) {
        FlowGraph graph = new FlowGraph(eventId, "Workflow");
        FlowNode start = new FlowNode(IDGenerator.newId("node"),
                FlowNodeType.START, "Start");
        start.setX(40);
        start.setY(40);
        FlowNode end = new FlowNode(IDGenerator.newId("node"),
                FlowNodeType.END, "End");
        end.setX(40);
        end.setY(220);
        graph.addNode(start);
        graph.addNode(end);
        graph.addConnection(new FlowConnection(IDGenerator.newId("conn"),
                start.getId(), FlowConnection.PORT_NEXT, end.getId()));
        return graph;
    }

    public void saveGraph(String projectId, String eventId, FlowGraph graph) {
        repository.saveFlowGraph(projectId, eventId,
                FlowSerializer.toJson(graph));
    }

    public void deleteGraph(String projectId, String eventId) {
        repository.deleteFlowGraph(projectId, eventId);
    }

    public List<String> validate(FlowGraph graph) {
        return FlowValidator.validate(graph);
    }

    public List<String> validateWarnings(FlowGraph graph) {
        return FlowValidator.validateWarnings(graph);
    }

    /** Runs the graph with a fresh context. Invalid graphs are refused. */
    public FlowExecutionResult run(String projectId, String screenId,
                                   String componentId, FlowGraph graph) {
        FlowExecutionContext context = new FlowExecutionContext();
        EventExecutionContext eventContext = new EventExecutionContext(
                projectId, screenId, componentId);
        eventContext.setFlowContext(context);
        return engine.execute(graph, context, eventContext);
    }

    /** One debug step against a context the caller keeps. */
    public FlowExecutionResult step(FlowGraph graph,
                                    FlowExecutionContext context,
                                    EventExecutionContext eventContext) {
        return engine.executeStep(graph, context, eventContext);
    }
}
