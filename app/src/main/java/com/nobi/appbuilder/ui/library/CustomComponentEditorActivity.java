package com.nobi.appbuilder.ui.library;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.library.custom.CustomComponentDefinition;
import com.nobi.appbuilder.library.custom.CustomComponentManager;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Saves the selected components of a screen as a reusable custom
 * component. Public properties are derived automatically; the
 * definition is versioned in SQLite.
 */
public class CustomComponentEditorActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_SCREEN_ID = "screen_id";
    public static final String EXTRA_COMPONENT_IDS = "component_ids";

    private String projectId;
    private String screenId;
    private ArrayList<String> componentIds;
    private DatabaseHelper dbHelper;
    private CustomComponentManager manager;
    private ComponentModel selectedRoot;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_component_editor);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        screenId = getIntent().getStringExtra(EXTRA_SCREEN_ID);
        componentIds = getIntent().getStringArrayListExtra(EXTRA_COMPONENT_IDS);
        if (projectId == null || screenId == null
                || componentIds == null || componentIds.isEmpty()) {
            Toast.makeText(this, R.string.library_no_selection,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_custom_component);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.library_custom_title);
        }
        dbHelper = new DatabaseHelper(this);
        manager = new CustomComponentManager(dbHelper);
        Button save = findViewById(R.id.btn_custom_component_save);
        save.setOnClickListener(v -> saveCustomComponent());
        loadSelection();
    }

    private void loadSelection() {
        executor.execute(() -> {
            DocumentRepository repo = new DocumentRepository(dbHelper);
            IRDocument document = repo.loadDocument(projectId);
            ScreenModel screen = document == null ? null
                    : document.getScreen(screenId);
            ComponentModel root = screen == null ? null
                    : screen.getRootComponent();
            ComponentModel found = null;
            if (root != null) {
                if (componentIds.size() == 1) {
                    found = root.findById(componentIds.get(0));
                } else {
                    found = root;
                }
            }
            final ComponentModel selected = found;
            mainHandler.post(() -> {
                selectedRoot = selected;
                TextView summary =
                        findViewById(R.id.text_custom_component_summary);
                if (selected == null) {
                    summary.setText(R.string.library_no_selection);
                    return;
                }
                summary.setText(getString(
                        R.string.library_custom_summary,
                        selected.getType(), componentIds.size()));
            });
        });
    }

    private void saveCustomComponent() {
        EditText nameInput = findViewById(R.id.input_custom_component_name);
        EditText descInput =
                findViewById(R.id.input_custom_component_description);
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.library_custom_name_required,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedRoot == null) {
            Toast.makeText(this, R.string.library_no_selection,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        final String description = descInput.getText().toString().trim();
        executor.execute(() -> {
            List<String> errors = new ArrayList<>();
            CustomComponentDefinition definition = manager.create(
                    projectId, name, description, selectedRoot, errors);
            mainHandler.post(() -> {
                if (definition == null) {
                    Toast.makeText(this,
                            errors.isEmpty() ? getString(R.string.library_custom_save_failed)
                                    : errors.get(0),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(this, R.string.library_custom_saved,
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
