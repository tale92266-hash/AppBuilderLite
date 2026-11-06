package com.nobi.appbuilder.ui.assets;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.assets.drawable.DrawableManager;
import com.nobi.appbuilder.assets.drawable.DrawableModel;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Creates shape and gradient drawables from a small form. */
public class DrawableEditorActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private String projectId;
    private DatabaseHelper dbHelper;
    private DrawableManager manager;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawable_editor);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.asset_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_drawable_editor);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.asset_drawable_editor_title);
        }
        dbHelper = new DatabaseHelper(this);
        manager = new DrawableManager(dbHelper);
        Spinner type = findViewById(R.id.spinner_drawable_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Shape", "Gradient"});
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);
        type.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent,
                                       View view, int position, long id) {
                findViewById(R.id.input_drawable_stroke_width)
                        .setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                findViewById(R.id.input_drawable_stroke_color)
                        .setVisibility(position == 0 ? View.VISIBLE : View.GONE);
                findViewById(R.id.input_drawable_radius)
                        .setVisibility(position == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        Button save = findViewById(R.id.btn_drawable_save);
        save.setOnClickListener(v -> saveDrawable());
    }

    private String textOf(int id) {
        return ((EditText) findViewById(id)).getText().toString().trim();
    }

    private int intOf(int id, int fallback) {
        try {
            return Integer.parseInt(textOf(id));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private void saveDrawable() {
        String name = textOf(R.id.input_drawable_name);
        if (name.isEmpty()) {
            Toast.makeText(this, R.string.asset_name_required,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Spinner type = findViewById(R.id.spinner_drawable_type);
        boolean isShape = type.getSelectedItemPosition() == 0;
        String solid = textOf(R.id.input_drawable_solid);
        String end = textOf(R.id.input_drawable_end);
        if (isShape && solid.isEmpty()) {
            Toast.makeText(this, R.string.asset_drawable_color_required,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isShape && (solid.isEmpty() || end.isEmpty())) {
            Toast.makeText(this, R.string.asset_drawable_color_required,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            DrawableModel created;
            if (isShape) {
                created = manager.createShape(projectId, name, "rectangle",
                        solid,
                        intOf(R.id.input_drawable_stroke_width, 0) == 0
                                ? null : intOf(R.id.input_drawable_stroke_width, 0),
                        textOf(R.id.input_drawable_stroke_color).isEmpty()
                                ? null : textOf(R.id.input_drawable_stroke_color),
                        intOf(R.id.input_drawable_radius, 0));
            } else {
                created = manager.createGradient(projectId, name, "linear",
                        90, solid, null, end);
            }
            mainHandler.post(() -> {
                if (created == null) {
                    Toast.makeText(this, R.string.asset_drawable_failed,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this, R.string.asset_drawable_saved,
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
