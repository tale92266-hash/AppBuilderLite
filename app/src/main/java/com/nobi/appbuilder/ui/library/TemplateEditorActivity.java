package com.nobi.appbuilder.ui.library;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.library.templates.TemplateCategory;
import com.nobi.appbuilder.library.templates.TemplateDefinition;
import com.nobi.appbuilder.library.templates.TemplateManager;
import com.nobi.appbuilder.library.templates.TemplateValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Edits a personal template's name, category and description. */
public class TemplateEditorActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_TEMPLATE_ID = "template_id";

    private String projectId;
    private String templateId;
    private DatabaseHelper dbHelper;
    private TemplateManager manager;
    private TemplateDefinition template;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_editor);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        templateId = getIntent().getStringExtra(EXTRA_TEMPLATE_ID);
        if (projectId == null || templateId == null) {
            Toast.makeText(this, R.string.library_no_template,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_template_editor);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.library_template_edit_title);
        }
        dbHelper = new DatabaseHelper(this);
        manager = new TemplateManager(this, dbHelper);
        Button save = findViewById(R.id.btn_template_editor_save);
        save.setOnClickListener(v -> saveTemplate());
        Button delete = findViewById(R.id.btn_template_editor_delete);
        delete.setOnClickListener(v -> deleteTemplate());
        loadTemplate();
    }

    private void loadTemplate() {
        executor.execute(() -> {
            TemplateDefinition loaded = manager.getRepository().getById(templateId);
            mainHandler.post(() -> {
                template = loaded;
                if (template == null) {
                    Toast.makeText(this, R.string.library_no_template,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                EditText name = findViewById(R.id.input_template_editor_name);
                EditText description =
                        findViewById(R.id.input_template_editor_description);
                Spinner category =
                        findViewById(R.id.spinner_template_editor_category);
                name.setText(template.getName());
                description.setText(template.getDescription());
                List<String> labels = new ArrayList<>();
                for (TemplateCategory c : TemplateCategory.values()) {
                    labels.add(c.getDisplayName());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, labels);
                adapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item);
                category.setAdapter(adapter);
                category.setSelection(template.getCategory() == null
                        ? 0 : template.getCategory().ordinal());
            });
        });
    }

    private void saveTemplate() {
        if (template == null) {
            return;
        }
        EditText nameInput = findViewById(R.id.input_template_editor_name);
        EditText descInput =
                findViewById(R.id.input_template_editor_description);
        Spinner categorySpinner =
                findViewById(R.id.spinner_template_editor_category);
        String name = nameInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.library_template_name_required,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        template.setName(name);
        template.setDescription(descInput.getText().toString().trim());
        template.setCategory(TemplateCategory.values()[
                categorySpinner.getSelectedItemPosition()]);
        List<String> problems = TemplateValidator.validate(template);
        if (!problems.isEmpty()) {
            Toast.makeText(this, problems.get(0), Toast.LENGTH_LONG).show();
            return;
        }
        executor.execute(() -> {
            boolean ok = manager.getRepository().save(template);
            mainHandler.post(() -> {
                if (!ok) {
                    Toast.makeText(this, R.string.library_template_failed,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, R.string.library_template_saved,
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private void deleteTemplate() {
        if (template == null || template.isBuiltIn()) {
            Toast.makeText(this, R.string.library_template_builtin,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            manager.delete(templateId);
            mainHandler.post(() -> {
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
