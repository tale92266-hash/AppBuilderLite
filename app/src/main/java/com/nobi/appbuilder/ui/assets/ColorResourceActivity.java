package com.nobi.appbuilder.ui.assets;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
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
import com.nobi.appbuilder.assets.colors.ColorResourceManager;
import com.nobi.appbuilder.assets.colors.ColorResourceModel;
import com.nobi.appbuilder.assets.colors.GradientModel;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Colour + gradient resources. Colours come from the Phase 12
 * design system; gradients are saved in color_resources.
 */
public class ColorResourceActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private String projectId;
    private DatabaseHelper dbHelper;
    private ColorResourceManager manager;
    private ColorAdapter adapter;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_resources);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.asset_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_color_resources);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.asset_colors_title);
        }
        dbHelper = new DatabaseHelper(this);
        manager = new ColorResourceManager(this, dbHelper);
        adapter = new ColorAdapter();
        RecyclerView list = findViewById(R.id.recycler_color_resources);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        Button addGradient = findViewById(R.id.btn_color_add_gradient);
        addGradient.setOnClickListener(v -> showAddGradientDialog());
        loadColors();
    }

    private void loadColors() {
        executor.execute(() -> {
            List<ColorResourceModel> colors = manager.getColors(projectId);
            List<GradientModel> gradients = manager.getGradients(projectId);
            mainHandler.post(() -> adapter.setItems(colors, gradients));
        });
    }

    private void showAddGradientDialog() {
        android.widget.LinearLayout root =
                new android.widget.LinearLayout(this);
        root.setOrientation(android.widget.LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        root.setPadding(padding, padding, padding, padding);
        EditText name = new EditText(this);
        name.setHint(R.string.asset_gradient_name_hint);
        EditText start = new EditText(this);
        start.setHint(R.string.asset_gradient_start_hint);
        EditText end = new EditText(this);
        end.setHint(R.string.asset_gradient_end_hint);
        root.addView(name);
        root.addView(start);
        root.addView(end);
        new AlertDialog.Builder(this)
                .setTitle(R.string.asset_gradient_title)
                .setView(root)
                .setPositiveButton(R.string.asset_create, (dialog, which) -> {
                    GradientModel gradient = new GradientModel();
                    gradient.setName(name.getText().toString().trim());
                    gradient.setType("linear");
                    gradient.setAngle(90);
                    gradient.setStartColor(start.getText().toString().trim());
                    gradient.setEndColor(end.getText().toString().trim());
                    executor.execute(() -> {
                        boolean ok = manager.saveGradient(projectId, gradient);
                        mainHandler.post(() -> {
                            if (!ok) {
                                Toast.makeText(this,
                                        R.string.asset_gradient_failed,
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Toast.makeText(this,
                                    R.string.asset_gradient_saved,
                                    Toast.LENGTH_SHORT).show();
                            loadColors();
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

    private class ColorAdapter
            extends RecyclerView.Adapter<ColorAdapter.Holder> {

        private final List<String> rows = new ArrayList<>();

        void setItems(List<ColorResourceModel> colors,
                      List<GradientModel> gradients) {
            rows.clear();
            for (ColorResourceModel color : colors) {
                rows.add(color.getName() + " · " + color.getHex());
            }
            for (GradientModel gradient : gradients) {
                rows.add(gradient.getName() + " · gradient · "
                        + gradient.getStartColor() + " → "
                        + gradient.getEndColor());
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_asset, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            holder.name.setText(rows.get(position));
            holder.detail.setText("");
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView detail;

            Holder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.text_asset_name);
                detail = itemView.findViewById(R.id.text_asset_detail);
            }
        }
    }
}
