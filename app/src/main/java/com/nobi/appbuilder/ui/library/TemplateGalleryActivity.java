package com.nobi.appbuilder.ui.library;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.library.templates.TemplateDefinition;
import com.nobi.appbuilder.library.templates.TemplateManager;
import com.nobi.appbuilder.library.templates.TemplatePreviewManager;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Browse starter templates and create a screen from one. */
public class TemplateGalleryActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_SCREEN_ID = "screen_id";
    public static final String EXTRA_NEW_SCREEN_ID = "new_screen_id";

    private String projectId;
    private String currentScreenId;
    private DatabaseHelper dbHelper;
    private TemplateManager manager;
    private TemplateAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template_gallery);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        currentScreenId = getIntent().getStringExtra(EXTRA_SCREEN_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.library_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_template_gallery);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.library_templates_title);
        }
        dbHelper = new DatabaseHelper(this);
        manager = new TemplateManager(this, dbHelper);
        adapter = new TemplateAdapter();
        RecyclerView list = findViewById(R.id.recycler_templates);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        Button saveCurrent = findViewById(R.id.btn_template_save_current);
        saveCurrent.setVisibility(currentScreenId == null
                ? View.GONE : View.VISIBLE);
        saveCurrent.setOnClickListener(v -> showSaveCurrentDialog());
        loadTemplates();
    }

    private void loadTemplates() {
        executor.execute(() -> {
            List<TemplateDefinition> templates = manager.getAll(projectId);
            mainHandler.post(() -> adapter.setItems(templates));
        });
    }

    private void useTemplate(TemplateDefinition template) {
        executor.execute(() -> {
            List<String> errors = new ArrayList<>();
            ScreenModel screen = manager.createScreenFromTemplate(
                    projectId, template, errors);
            mainHandler.post(() -> {
                if (screen == null) {
                    Toast.makeText(this,
                            errors.isEmpty() ? getString(R.string.library_template_failed)
                                    : errors.get(0),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(this, R.string.library_template_created,
                        Toast.LENGTH_SHORT).show();
                getIntent().putExtra(EXTRA_NEW_SCREEN_ID, screen.getId());
                setResult(RESULT_OK, getIntent());
                finish();
            });
        });
    }

    private void showSaveCurrentDialog() {
        EditText nameInput = new EditText(this);
        nameInput.setHint(R.string.library_template_name_hint);
        new AlertDialog.Builder(this)
                .setTitle(R.string.library_template_save_title)
                .setView(nameInput)
                .setPositiveButton(R.string.library_save, (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this,
                                R.string.library_template_name_required,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    executor.execute(() -> {
                        List<String> errors = new ArrayList<>();
                        TemplateDefinition created = manager.createFromScreen(
                                projectId, currentScreenId, name,
                                com.nobi.appbuilder.library.templates.TemplateCategory.EMPTY,
                                errors);
                        mainHandler.post(() -> {
                            if (created == null) {
                                Toast.makeText(this,
                                        errors.isEmpty() ? getString(R.string.library_template_failed)
                                                : errors.get(0),
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            Toast.makeText(this,
                                    R.string.library_template_saved,
                                    Toast.LENGTH_SHORT).show();
                            loadTemplates();
                        });
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
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

    private class TemplateAdapter
            extends RecyclerView.Adapter<TemplateAdapter.Holder> {

        private final List<TemplateDefinition> items = new ArrayList<>();

        void setItems(List<TemplateDefinition> templates) {
            items.clear();
            items.addAll(templates);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_template, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            TemplateDefinition template = items.get(position);
            holder.name.setText(template.getName());
            holder.detail.setText(
                    TemplatePreviewManager.previewSummary(template));
            holder.use.setOnClickListener(v -> useTemplate(template));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView detail;
            final Button use;

            Holder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.text_template_name);
                detail = itemView.findViewById(R.id.text_template_detail);
                use = itemView.findViewById(R.id.btn_template_use);
            }
        }
    }
}
