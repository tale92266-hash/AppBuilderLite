package com.nobi.appbuilder.ui.flow;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.nobi.appbuilder.R;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.flow.FlowConnection;
import com.nobi.appbuilder.flow.FlowEditorManager;
import com.nobi.appbuilder.flow.FlowExecutionResult;
import com.nobi.appbuilder.flow.FlowGraph;
import com.nobi.appbuilder.flow.FlowNode;
import com.nobi.appbuilder.flow.FlowNodeType;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * The workflow editor for one event: canvas on top, node list below,
 * toolbar (Run / Validate / Save / Debug) and a properties panel for
 * the selected node. Adding a node connects it from the selected
 * node (port "next", or "true" for a condition source); deleting a
 * node removes its connections too.
 */
public class FlowEditorActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_SCREEN_ID = "screen_id";
    public static final String EXTRA_COMPONENT_ID = "component_id";
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_TRIGGER = "trigger";

    private String projectId;
    private String screenId;
    private String componentId;
    private String eventId;
    private FlowEditorManager manager;
    private FlowGraph graph;
    private FlowCanvasView canvas;
    private FlowPropertiesController properties;
    private NodeAdapter adapter;
    private FlowNode selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flow_editor);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        screenId = getIntent().getStringExtra(EXTRA_SCREEN_ID);
        componentId = getIntent().getStringExtra(EXTRA_COMPONENT_ID);
        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (projectId == null || eventId == null) {
            Toast.makeText(this, R.string.flow_no_event, Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }
        manager = new FlowEditorManager(new DatabaseHelper(this));
        graph = manager.loadGraph(projectId, eventId);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_flow_editor);
        toolbar.setNavigationOnClickListener(v -> finish());

        canvas = findViewById(R.id.canvas_flow);
        canvas.setGraph(graph);
        canvas.setOnNodeSelectedListener(node -> selectNode(node));

        RecyclerView list = findViewById(R.id.recycler_flow_nodes);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NodeAdapter();
        list.setAdapter(adapter);

        View toolbarRow = findViewById(R.id.layout_flow_toolbar);
        new FlowToolbarController(toolbarRow,
                new FlowToolbarController.FlowToolbarListener() {
                    @Override
                    public void onRun() {
                        runFlow();
                    }

                    @Override
                    public void onValidate() {
                        showValidationDialog();
                    }

                    @Override
                    public void onSave() {
                        saveGraph();
                    }

                    @Override
                    public void onDebug() {
                        new FlowDebugController(FlowEditorActivity.this,
                                projectId, screenId, componentId, graph,
                                manager).show();
                    }
                });

        View propertiesRoot = findViewById(R.id.layout_flow_properties);
        properties = new FlowPropertiesController(propertiesRoot,
                node -> {
                    canvas.setGraph(graph);
                    adapter.refresh();
                });

        Button addNode = findViewById(R.id.btn_flow_add_node);
        addNode.setOnClickListener(v -> showAddNodeDialog());
        Button deleteNode = findViewById(R.id.btn_flow_delete_node);
        deleteNode.setOnClickListener(v -> deleteSelectedNode());
        adapter.refresh();
    }

    private void selectNode(FlowNode node) {
        selected = node;
        canvas.setSelectedNodeId(node == null ? null : node.getId());
        if (node == null) {
            properties.hide();
        } else {
            properties.show(node);
        }
        adapter.refresh();
    }

    private void showAddNodeDialog() {
        List<String> types = new ArrayList<>();
        for (FlowNodeType type : FlowNodeType.values()) {
            if (type != FlowNodeType.START && type != FlowNodeType.END) {
                types.add(type.name());
            }
        }
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        new AlertDialog.Builder(this)
                .setTitle(R.string.flow_add_node)
                .setView(spinner)
                .setPositiveButton(R.string.flow_add, (d, w) -> {
                    FlowNodeType type = FlowNodeType.fromName(
                            (String) spinner.getSelectedItem());
                    addNodeOfType(type);
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void addNodeOfType(FlowNodeType type) {
        FlowNode node = new FlowNode(IDGenerator.newId("node"), type,
                type.displayName());
        float y = 40 + graph.getNodes().size() * 110;
        node.setX(40);
        node.setY(y);
        if (type == FlowNodeType.ACTION) {
            node.setActionId("showToast");
            node.setParam("message", "Done");
        } else if (type == FlowNodeType.CONDITION) {
            node.setParam("operator", "equals");
            node.setParam("left", "");
            node.setParam("right", "");
        } else if (type == FlowNodeType.VARIABLE) {
            node.setParam("name", "value");
            node.setParam("value", "");
            node.setParam("type", "string");
        }
        graph.addNode(node);
        FlowNode from = selected != null ? selected : graph.getStartNode();
        if (from != null && from.getType() != FlowNodeType.END) {
            String port = from.getType() == FlowNodeType.CONDITION
                    ? FlowConnection.PORT_TRUE : FlowConnection.PORT_NEXT;
            graph.addConnection(new FlowConnection(
                    IDGenerator.newId("conn"), from.getId(), port,
                    node.getId()));
        }
        canvas.setGraph(graph);
        adapter.refresh();
        selectNode(node);
    }

    private void deleteSelectedNode() {
        if (selected == null) {
            return;
        }
        if (selected.getType() == FlowNodeType.START
                || selected.getType() == FlowNodeType.END) {
            Toast.makeText(this, R.string.flow_cannot_delete_endpoints,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        graph.removeNode(selected.getId());
        selectNode(null);
        canvas.setGraph(graph);
        adapter.refresh();
    }

    private void saveGraph() {
        manager.saveGraph(projectId, eventId, graph);
        Toast.makeText(this, R.string.flow_saved, Toast.LENGTH_SHORT).show();
    }

    private void runFlow() {
        FlowExecutionResult result = manager.run(projectId, screenId,
                componentId, graph);
        StringBuilder detail = new StringBuilder();
        if (result.isSuccess()) {
            detail.append(getString(R.string.flow_run_ok));
        } else {
            detail.append("Error: ").append(result.getError());
        }
        detail.append("\n");
        for (String line : result.getLog()) {
            detail.append(line).append("\n");
        }
        showDetailDialog(getString(R.string.flow_run_title),
                detail.toString().trim());
    }

    private void showValidationDialog() {
        List<String> errors = manager.validate(graph);
        List<String> warnings = manager.validateWarnings(graph);
        StringBuilder detail = new StringBuilder();
        if (errors.isEmpty() && warnings.isEmpty()) {
            detail.append(getString(R.string.flow_validation_ok));
        }
        for (String error : errors) {
            detail.append("Error: ").append(error).append("\n");
        }
        for (String warning : warnings) {
            detail.append("Warning: ").append(warning).append("\n");
        }
        showDetailDialog(getString(R.string.flow_validation_title),
                detail.toString().trim());
    }

    private void showDetailDialog(String title, String detail) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_flow_validation, null);
        TextView titleView = view.findViewById(R.id.text_flow_validation_title);
        titleView.setText(title);
        TextView text = view.findViewById(R.id.text_flow_validation_detail);
        text.setText(detail);
        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.Holder> {
        private final List<FlowNode> nodes = new ArrayList<>();

        void refresh() {
            nodes.clear();
            nodes.addAll(graph.getNodes());
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_flow_node, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            FlowNode node = nodes.get(position);
            holder.title.setText(node.getType().displayName() + ": "
                    + node.getLabel());
            holder.detail.setText(node.getActionId() == null
                    ? "" : node.getActionId());
            holder.itemView.setSelected(selected != null
                    && selected.getId() != null
                    && selected.getId().equals(node.getId()));
            holder.itemView.setOnClickListener(v -> selectNode(node));
        }

        @Override
        public int getItemCount() {
            return nodes.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView detail;

            Holder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.text_flow_node_title);
                detail = itemView.findViewById(R.id.text_flow_node_detail);
            }
        }
    }
}
