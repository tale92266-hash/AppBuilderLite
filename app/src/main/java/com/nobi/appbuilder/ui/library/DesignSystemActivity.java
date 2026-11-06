package com.nobi.appbuilder.ui.library;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.library.design.DesignSystemManager;
import com.nobi.appbuilder.library.design.DesignToken;
import com.nobi.appbuilder.library.design.ThemeDefinition;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Design system screen: colour tokens (tap to edit the hex value)
 * and themes (apply to the project, or edit in a dialog). Applying
 * a theme updates the JSON IR theme only — explicit component
 * colours are never overwritten.
 */
public class DesignSystemActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private String projectId;
    private DatabaseHelper dbHelper;
    private DesignSystemManager manager;
    private DesignTokenController tokenController;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_design_system);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.library_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_design_system);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.library_design_title);
        }
        dbHelper = new DatabaseHelper(this);
        manager = new DesignSystemManager(this, dbHelper);
        tokenController = new DesignTokenController(token ->
                showEditColorDialog(token));
        RecyclerView tokens = findViewById(R.id.recycler_design_tokens);
        tokens.setLayoutManager(new LinearLayoutManager(this));
        tokens.setAdapter(tokenController);
        Button newTheme = findViewById(R.id.btn_design_new_theme);
        newTheme.setOnClickListener(v -> {
            ThemeDefinition theme = ThemeDefinition.light(
                    IDGenerator.newId(), projectId, "Custom theme");
            new ThemeEditorController(this, saved -> {
                executor.execute(() -> {
                    manager.getThemeRepository().save(saved);
                    mainHandler.post(this::loadAll);
                });
            }).show(theme);
        });
        loadAll();
    }

    private void loadAll() {
        executor.execute(() -> {
            List<DesignToken> colors = manager.getTokens(projectId,
                    DesignToken.KIND_COLOR);
            List<ThemeDefinition> themes = manager.getThemes(projectId);
            mainHandler.post(() -> {
                tokenController.setTokens(colors);
                renderThemes(themes);
            });
        });
    }

    private void renderThemes(List<ThemeDefinition> themes) {
        LinearLayout container = findViewById(R.id.layout_design_themes);
        container.removeAllViews();
        for (ThemeDefinition theme : themes) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            Button apply = new Button(this);
            apply.setText(getString(R.string.library_theme_apply,
                    theme.getName()));
            apply.setOnClickListener(v -> applyTheme(theme));
            Button edit = new Button(this);
            edit.setText(R.string.library_edit);
            edit.setOnClickListener(v -> new ThemeEditorController(this,
                    saved -> executor.execute(() -> {
                        manager.getThemeRepository().save(saved);
                        mainHandler.post(this::loadAll);
                    })).show(theme));
            row.addView(apply);
            row.addView(edit);
            container.addView(row);
        }
    }

    private void applyTheme(ThemeDefinition theme) {
        executor.execute(() -> {
            boolean ok = manager.applyTheme(projectId, theme);
            mainHandler.post(() -> Toast.makeText(this,
                    ok ? R.string.library_theme_applied
                            : R.string.library_theme_failed,
                    Toast.LENGTH_SHORT).show());
        });
    }

    private void showEditColorDialog(DesignToken token) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(token.getValue());
        new AlertDialog.Builder(this)
                .setTitle(token.getName())
                .setView(input)
                .setPositiveButton(R.string.library_save, (dialog, which) -> {
                    String hex = input.getText().toString().trim();
                    executor.execute(() -> {
                        boolean ok = manager.updateColor(projectId,
                                token.getName(), hex);
                        mainHandler.post(() -> {
                            if (!ok) {
                                Toast.makeText(this,
                                        R.string.library_color_invalid,
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            loadAll();
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
}
