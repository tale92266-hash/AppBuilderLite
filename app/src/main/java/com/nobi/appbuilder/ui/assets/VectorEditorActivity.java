package com.nobi.appbuilder.ui.assets;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.assets.vector.VectorAssetManager;
import com.nobi.appbuilder.assets.vector.VectorAssetModel;
import com.nobi.appbuilder.assets.vector.VectorPathModel;
import com.nobi.appbuilder.assets.vector.VectorValidator;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Creates a vector drawable: viewport, size, one path + paint. */
public class VectorEditorActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private String projectId;
    private DatabaseHelper dbHelper;
    private VectorAssetManager manager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vector_editor);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.asset_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_vector_editor);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.asset_vector_editor_title);
        }
        dbHelper = new DatabaseHelper(this);
        manager = new VectorAssetManager(dbHelper);
        Button save = findViewById(R.id.btn_vector_save);
        save.setOnClickListener(v -> saveVector());
    }

    private String textOf(int id) {
        return ((EditText) findViewById(id)).getText().toString().trim();
    }

    private float floatOf(int id, float fallback) {
        try {
            return Float.parseFloat(textOf(id));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void saveVector() {
        String name = textOf(R.id.input_vector_name);
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.asset_name_required,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        VectorPathModel path = new VectorPathModel();
        path.setPathData(textOf(R.id.input_vector_path));
        path.setFillColor(textOf(R.id.input_vector_fill).isEmpty()
                ? null : textOf(R.id.input_vector_fill));
        path.setStrokeColor(textOf(R.id.input_vector_stroke).isEmpty()
                ? null : textOf(R.id.input_vector_stroke));
        path.setStrokeWidth(floatOf(R.id.input_vector_stroke_width, 0));
        VectorAssetModel model = new VectorAssetModel();
        model.setId(IDGenerator.newId());
        model.setProjectId(projectId);
        model.setName(name);
        model.setViewportWidth(floatOf(R.id.input_vector_viewport_w, 24));
        model.setViewportHeight(floatOf(R.id.input_vector_viewport_h, 24));
        model.setWidthDp(floatOf(R.id.input_vector_width, 24));
        model.setHeightDp(floatOf(R.id.input_vector_height, 24));
        List<VectorPathModel> paths = new ArrayList<>();
        paths.add(path);
        model.setPaths(paths);
        model.setCreatedAt(System.currentTimeMillis());
        List<String> problems = VectorValidator.validate(model);
        if (!problems.isEmpty()) {
            Toast.makeText(this, problems.get(0), Toast.LENGTH_LONG).show();
            return;
        }
        executor.execute(() -> {
            boolean ok = manager.save(model);
            mainHandler.post(() -> {
                if (!ok) {
                    Toast.makeText(this, R.string.asset_vector_failed,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, R.string.asset_vector_saved,
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
