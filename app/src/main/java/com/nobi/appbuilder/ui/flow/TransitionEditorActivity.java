package com.nobi.appbuilder.ui.flow;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.nobi.appbuilder.R;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.navigation.NavigationEdge;
import com.nobi.appbuilder.navigation.NavigationGraph;
import com.nobi.appbuilder.navigation.NavigationManager;
import com.nobi.appbuilder.transitions.TransitionDefinition;
import com.nobi.appbuilder.transitions.TransitionRegistry;
import com.nobi.appbuilder.transitions.TransitionValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Edits the transition of one navigation edge: type + custom duration
 * (100–1000ms, validated). Framework anims run at their own fixed
 * timing in preview; the duration is stored and validated, and the
 * generated snippet documents it.
 */
public class TransitionEditorActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_EDGE_ID = "edge_id";
    public static final String EXTRA_FROM_SCREEN_ID = "from_screen_id";
    public static final String EXTRA_TO_SCREEN_ID = "to_screen_id";

    private String projectId;
    private String edgeId;
    private NavigationManager manager;
    private NavigationGraph graph;
    private NavigationEdge edge;
    private Spinner typeSpinner;
    private EditText durationInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition_editor);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        edgeId = getIntent().getStringExtra(EXTRA_EDGE_ID);
        String fromId = getIntent().getStringExtra(EXTRA_FROM_SCREEN_ID);
        String toId = getIntent().getStringExtra(EXTRA_TO_SCREEN_ID);
        if (projectId == null || edgeId == null) {
            Toast.makeText(this, R.string.flow_no_edge, Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }
        manager = new NavigationManager(new DatabaseHelper(this));
        graph = manager.getGraph(projectId);
        edge = graph.getEdge(edgeId);
        if (edge == null) {
            Toast.makeText(this, R.string.flow_no_edge, Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar_transition_editor);
        toolbar.setNavigationOnClickListener(v -> finish());

        TextView detail = findViewById(R.id.text_transition_detail);
        detail.setText(getString(R.string.flow_transition_detail,
                nameFor(fromId), nameFor(toId)));

        typeSpinner = findViewById(R.id.spinner_transition_type);
        List<String> names = new ArrayList<>();
        List<String> ids = TransitionRegistry.getIds();
        for (String id : ids) {
            names.add(TransitionRegistry.get(id).getDisplayName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter);
        int selected = ids.indexOf(edge.getTransitionId());
        typeSpinner.setSelection(selected < 0 ? 0 : selected);

        durationInput = findViewById(R.id.input_transition_duration);
        durationInput.setText(String.valueOf(edge.getDurationMs()));

        Button save = findViewById(R.id.btn_transition_save);
        save.setOnClickListener(v -> saveTransition());
    }

    private String nameFor(String screenId) {
        for (com.nobi.appbuilder.model.ScreenModel screen
                : manager.getScreens(projectId)) {
            if (screenId != null && screenId.equals(screen.getId())) {
                return screen.getName();
            }
        }
        return screenId == null ? "?" : screenId;
    }

    private void saveTransition() {
        String transitionId = TransitionRegistry.getIds()
                .get(typeSpinner.getSelectedItemPosition());
        int duration;
        try {
            duration = Integer.parseInt(
                    durationInput.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.flow_transition_duration_invalid,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> problems = TransitionValidator.validate(transitionId,
                duration);
        if (!problems.isEmpty()) {
            Toast.makeText(this, problems.get(0), Toast.LENGTH_SHORT).show();
            return;
        }
        manager.setTransition(projectId, graph, edgeId, transitionId);
        edge.setDurationMs(TransitionDefinition.clampDuration(duration));
        manager.saveGraph(projectId, graph);
        Toast.makeText(this, R.string.flow_transition_saved,
                Toast.LENGTH_SHORT).show();
        finish();
    }
}
