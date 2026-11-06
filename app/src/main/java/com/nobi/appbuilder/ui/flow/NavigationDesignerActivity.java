package com.nobi.appbuilder.ui.flow;

import android.app.AlertDialog;
import android.content.Intent;
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
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.navigation.NavigationEdge;
import com.nobi.appbuilder.navigation.NavigationGraph;
import com.nobi.appbuilder.navigation.NavigationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * The navigation designer: a graph diagram on top, the edge list
 * below. Add an edge (from/to screens), tap one to edit its
 * transition, long-press to delete, Validate to see errors and
 * warnings, Rebuild to re-derive edges from navigate events while
 * keeping hand-drawn edges.
 */
public class NavigationDesignerActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private String projectId;
    private NavigationManager manager;
    private NavigationGraph graph;
    private NavigationGraphView graphView;
    private EdgeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_designer);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.flow_no_project, Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        manager = new NavigationManager(dbHelper);
        graph = manager.getGraph(projectId);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_navigation_designer);
        toolbar.setNavigationOnClickListener(v -> finish());
        graphView = findViewById(R.id.graph_navigation_view);
        graphView.setGraph(graph);
        graphView.setOnScreenSelectedListener(screenId ->
                graphView.setSelectedScreenId(screenId));

        RecyclerView list = findViewById(R.id.recycler_nav_edges);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EdgeAdapter();
        list.setAdapter(adapter);

        Button addEdge = findViewById(R.id.btn_nav_add_edge);
        addEdge.setOnClickListener(v -> showAddEdgeDialog());
        Button validate = findViewById(R.id.btn_nav_validate);
        validate.setOnClickListener(v -> showValidationDialog());
        Button rebuild = findViewById(R.id.btn_nav_rebuild);
        rebuild.setOnClickListener(v -> {
            graph = manager.rebuild(projectId);
            graphView.setGraph(graph);
            adapter.refresh();
            Toast.makeText(this, R.string.flow_nav_rebuilt,
                    Toast.LENGTH_SHORT).show();
        });
        adapter.refresh();
    }

    private void showAddEdgeDialog() {
        List<ScreenModel> screens = manager.getScreens(projectId);
        List<String> names = new ArrayList<>();
        List<String> ids = new ArrayList<>();
        for (ScreenModel screen : screens) {
            names.add(screen.getName());
            ids.add(screen.getId());
        }
        if (names.size() < 2) {
            Toast.makeText(this, R.string.flow_nav_need_screens,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Spinner from = new Spinner(this);
        from.setAdapter(simpleAdapter(names));
        Spinner to = new Spinner(this);
        to.setAdapter(simpleAdapter(names));
        to.setSelection(1);
        android.widget.LinearLayout root = new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(padding, padding, padding, padding);
        root.addView(from);
        root.addView(to);
        new AlertDialog.Builder(this)
                .setTitle(R.string.flow_nav_add_edge)
                .setView(root)
                .setPositiveButton(R.string.flow_add, (d, w) -> {
                    String error = manager.addEdge(projectId, graph,
                            ids.get(from.getSelectedItemPosition()),
                            ids.get(to.getSelectedItemPosition()));
                    if (error != null) {
                        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                    } else {
                        graphView.setGraph(graph);
                        adapter.refresh();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showValidationDialog() {
        List<String> errors = manager.validate(projectId, graph);
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
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_flow_validation, null);
        TextView title = view.findViewById(R.id.text_flow_validation_title);
        title.setText(getString(R.string.flow_validation_title));
        TextView text = view.findViewById(R.id.text_flow_validation_detail);
        text.setText(detail.toString().trim());
        new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private ArrayAdapter<String> simpleAdapter(List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private class EdgeAdapter extends RecyclerView.Adapter<EdgeAdapter.Holder> {
        private final List<NavigationEdge> edges = new ArrayList<>();

        void refresh() {
            edges.clear();
            edges.addAll(graph.getEdges());
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_navigation_screen, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            NavigationEdge edge = edges.get(position);
            String from = nameFor(edge.getFromScreenId());
            String to = nameFor(edge.getToScreenId());
            holder.name.setText(from + " → " + to);
            holder.detail.setText(edge.getLabel()
                    + (edge.isDerivedFromEvent() ? " (from event)" : "")
                    + " · " + edge.getTransitionId());
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(NavigationDesignerActivity.this,
                        TransitionEditorActivity.class);
                intent.putExtra(TransitionEditorActivity.EXTRA_PROJECT_ID,
                        projectId);
                intent.putExtra(TransitionEditorActivity.EXTRA_EDGE_ID,
                        edge.getId());
                intent.putExtra(TransitionEditorActivity.EXTRA_FROM_SCREEN_ID,
                        edge.getFromScreenId());
                intent.putExtra(TransitionEditorActivity.EXTRA_TO_SCREEN_ID,
                        edge.getToScreenId());
                startActivity(intent);
            });
            holder.itemView.setOnLongClickListener(v -> {
                new AlertDialog.Builder(NavigationDesignerActivity.this)
                        .setTitle(R.string.flow_nav_delete_edge)
                        .setPositiveButton(R.string.flow_delete, (d, w) -> {
                            manager.removeEdge(projectId, graph, edge.getId());
                            graphView.setGraph(graph);
                            refresh();
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return true;
            });
        }

        private String nameFor(String screenId) {
            for (ScreenModel screen : manager.getScreens(projectId)) {
                if (screenId != null && screenId.equals(screen.getId())) {
                    return screen.getName();
                }
            }
            return screenId == null ? "?" : screenId;
        }

        @Override
        public int getItemCount() {
            return edges.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView detail;

            Holder(@NonNull View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.text_nav_screen_name);
                detail = itemView.findViewById(R.id.text_nav_screen_detail);
            }
        }
    }
}
