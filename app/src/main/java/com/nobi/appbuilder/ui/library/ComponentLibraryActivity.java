package com.nobi.appbuilder.ui.library;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.library.ComponentLibraryItem;
import com.nobi.appbuilder.library.ComponentLibraryManager;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Full component library screen. Pick a component, tap Add, and it
 * lands on the editor screen's root container. Unsupported entries
 * are shown with their reason and cannot be added.
 */
public class ComponentLibraryActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_SCREEN_ID = "screen_id";

    private String projectId;
    private String screenId;
    private DatabaseHelper dbHelper;
    private ComponentLibraryManager manager;
    private ComponentLibraryController controller;
    private ComponentDetailsController detailsController;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component_library);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        screenId = getIntent().getStringExtra(EXTRA_SCREEN_ID);
        if (projectId == null || screenId == null) {
            Toast.makeText(this, R.string.library_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_library);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.library_title);
        }
        dbHelper = new DatabaseHelper(this);
        manager = new ComponentLibraryManager(this, dbHelper);
        detailsController = new ComponentDetailsController(this,
                new ComponentDetailsController.DetailsListener() {
                    @Override
                    public void onAdd(ComponentLibraryItem item) {
                        addToCanvas(item);
                    }

                    @Override
                    public void onToggleFavorite(ComponentLibraryItem item) {
                        toggleFavorite(item);
                    }
                });
        controller = new ComponentLibraryController(
                findViewById(R.id.layout_library_root),
                new ComponentLibraryController.LibraryListener() {
                    @Override
                    public void onOpenDetails(ComponentLibraryItem item) {
                        detailsController.show(item);
                    }

                    @Override
                    public void onAdd(ComponentLibraryItem item) {
                        addToCanvas(item);
                    }

                    @Override
                    public void onToggleFavorite(ComponentLibraryItem item) {
                        toggleFavorite(item);
                    }
                });
        loadCatalog();
    }

    private void loadCatalog() {
        executor.execute(() -> {
            List<ComponentLibraryItem> items = manager.getCatalog(projectId);
            mainHandler.post(() -> controller.setItems(items));
        });
    }

    private void addToCanvas(ComponentLibraryItem item) {
        if (!item.isSupported()) {
            Toast.makeText(this,
                    item.getUnsupportedReason() == null
                            ? getString(R.string.library_unsupported)
                            : item.getUnsupportedReason(),
                    Toast.LENGTH_LONG).show();
            return;
        }
        executor.execute(() -> {
            ComponentModel created = manager.addToCanvas(projectId, screenId,
                    item, 16, 16);
            mainHandler.post(() -> {
                if (created == null) {
                    Toast.makeText(this, R.string.library_add_failed,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, R.string.library_added,
                        Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    private void toggleFavorite(ComponentLibraryItem item) {
        boolean next = !item.isFavorite();
        item.setFavorite(next);
        executor.execute(() -> manager.getRepository().setFavorite(
                projectId, item.getId(), next));
        controller.refresh();
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
