package com.nobi.appbuilder.ui.flow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.nobi.appbuilder.R;
import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionRegistry;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.events.EventDefinition;
import com.nobi.appbuilder.events.EventManager;
import com.nobi.appbuilder.events.EventTrigger;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.navigation.NavigationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Edits one event bound to a component: trigger, action, target
 * screen and message. Save validates first (bad events are refused
 * with a reason). "Open workflow" jumps to the flow editor for the
 * saved event; a new event must be saved first.
 */
public class EventEditorActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_SCREEN_ID = "screen_id";
    public static final String EXTRA_COMPONENT_ID = "component_id";
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_TRIGGER = "trigger";

    private String projectId;
    private String screenId;
    private String componentId;
    private String currentEventId;
    private EventManager manager;
    private EventActionAdapter adapter;
    private Spinner triggerSpinner;
    private Spinner actionSpinner;
    private Spinner targetSpinner;
    private EditText messageInput;
    private final List<String> targetIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_editor);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        screenId = getIntent().getStringExtra(EXTRA_SCREEN_ID);
        componentId = getIntent().getStringExtra(EXTRA_COMPONENT_ID);
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        String trigger = getIntent().getStringExtra(EXTRA_TRIGGER);
        if (projectId == null || screenId == null || componentId == null) {
            Toast.makeText(this, R.string.flow_no_event, Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        manager = new EventManager(dbHelper);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_event_editor);
        toolbar.setNavigationOnClickListener(v -> finish());

        triggerSpinner = findViewById(R.id.spinner_event_trigger);
        List<String> triggers = new ArrayList<>();
        for (String t : EventTrigger.COMPONENT_TRIGGERS) {
            triggers.add(EventTrigger.displayName(t));
        }
        triggerSpinner.setAdapter(simpleAdapter(triggers));
        if (trigger != null) {
            for (int i = 0; i < EventTrigger.COMPONENT_TRIGGERS.length; i++) {
                if (EventTrigger.COMPONENT_TRIGGERS[i].equals(trigger)) {
                    triggerSpinner.setSelection(i);
                    break;
                }
            }
        }

        actionSpinner = findViewById(R.id.spinner_event_action);
        List<String> actions = new ArrayList<>();
        for (ActionDefinition def : ActionRegistry.getAll()) {
            actions.add(def.getDisplayName());
        }
        actionSpinner.setAdapter(simpleAdapter(actions));

        targetSpinner = findViewById(R.id.spinner_event_target_screen);
        List<String> targets = new ArrayList<>();
        targets.add("—");
        targetIds.add("");
        NavigationManager nav = new NavigationManager(dbHelper);
        for (ScreenModel screen : nav.getScreens(projectId)) {
            targets.add(screen.getName());
            targetIds.add(screen.getId());
        }
        targetSpinner.setAdapter(simpleAdapter(targets));

        messageInput = findViewById(R.id.input_event_message);

        RecyclerView list = findViewById(R.id.recycler_event_actions);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventActionAdapter(definition -> {
            currentEventId = definition.getId();
            loadIntoForm(definition);
        });
        list.setAdapter(adapter);
        refreshList();

        if (currentEventId != null) {
            EventDefinition existing = manager.getEvent(projectId, screenId,
                    componentId, currentEventId);
            if (existing != null) {
                loadIntoForm(existing);
            }
        }

        Button save = findViewById(R.id.btn_event_save);
        save.setOnClickListener(v -> saveEvent());
        Button workflow = findViewById(R.id.btn_event_open_workflow);
        workflow.setOnClickListener(v -> openWorkflow());
        Button delete = findViewById(R.id.btn_event_delete);
        delete.setOnClickListener(v -> deleteEvent());
    }

    private void refreshList() {
        adapter.setItems(manager.getEvents(projectId, screenId, componentId));
    }

    private void loadIntoForm(EventDefinition definition) {
        for (int i = 0; i < EventTrigger.COMPONENT_TRIGGERS.length; i++) {
            if (EventTrigger.COMPONENT_TRIGGERS[i]
                    .equals(definition.getTrigger())) {
                triggerSpinner.setSelection(i);
                break;
            }
        }
        List<ActionDefinition> all = ActionRegistry.getAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(definition.getAction())) {
                actionSpinner.setSelection(i);
                break;
            }
        }
        int targetIndex = targetIds.indexOf(definition.getTargetScreenId());
        targetSpinner.setSelection(targetIndex < 0 ? 0 : targetIndex);
        messageInput.setText(definition.getMessage() == null
                ? "" : definition.getMessage());
    }

    private EventDefinition buildFromForm() {
        String trigger = EventTrigger.COMPONENT_TRIGGERS[
                triggerSpinner.getSelectedItemPosition()];
        String action = ActionRegistry.getAll()
                .get(actionSpinner.getSelectedItemPosition()).getId();
        String target = targetIds.get(targetSpinner.getSelectedItemPosition());
        String message = messageInput.getText().toString().trim();
        return new EventDefinition(currentEventId, componentId, trigger,
                action, target.isEmpty() ? null : target,
                message.isEmpty() ? null : message);
    }

    private void saveEvent() {
        EventDefinition definition = buildFromForm();
        String error;
        if (currentEventId == null) {
            error = manager.addEvent(projectId, screenId, componentId,
                    definition);
            if (error == null) {
                currentEventId = definition.getId();
            }
        } else {
            error = manager.updateEvent(projectId, screenId, componentId,
                    definition);
        }
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, R.string.flow_event_saved, Toast.LENGTH_SHORT)
                .show();
        refreshList();
    }

    private void openWorkflow() {
        if (currentEventId == null) {
            Toast.makeText(this, R.string.flow_save_event_first,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, FlowEditorActivity.class);
        intent.putExtra(FlowEditorActivity.EXTRA_PROJECT_ID, projectId);
        intent.putExtra(FlowEditorActivity.EXTRA_SCREEN_ID, screenId);
        intent.putExtra(FlowEditorActivity.EXTRA_COMPONENT_ID, componentId);
        intent.putExtra(FlowEditorActivity.EXTRA_EVENT_ID, currentEventId);
        intent.putExtra(FlowEditorActivity.EXTRA_TRIGGER,
                EventTrigger.COMPONENT_TRIGGERS[
                        triggerSpinner.getSelectedItemPosition()]);
        startActivity(intent);
    }

    private void deleteEvent() {
        if (currentEventId == null) {
            return;
        }
        String error = manager.removeEvent(projectId, screenId, componentId,
                currentEventId);
        if (error != null) {
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            return;
        }
        currentEventId = null;
        messageInput.setText("");
        refreshList();
        Toast.makeText(this, R.string.flow_event_deleted, Toast.LENGTH_SHORT)
                .show();
    }

    private ArrayAdapter<String> simpleAdapter(List<String> items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }
}
