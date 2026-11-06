package com.nobi.appbuilder.ui.assets;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.assets.fonts.FontAssetModel;
import com.nobi.appbuilder.assets.fonts.FontManager;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Font list + TTF/OTF import. Only formats Android can consume. */
public class FontManagerActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private String projectId;
    private DatabaseHelper dbHelper;
    private FontManager manager;
    private FontAdapter adapter;
    private ActivityResultLauncher<String> picker;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_manager);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.asset_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_font_manager);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.asset_fonts_title);
        }
        dbHelper = new DatabaseHelper(this);
        manager = new FontManager(this, dbHelper);
        adapter = new FontAdapter();
        RecyclerView list = findViewById(R.id.recycler_fonts);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);
        picker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        importFont(uri);
                    }
                });
        Button importButton = findViewById(R.id.btn_font_import);
        importButton.setOnClickListener(v -> picker.launch("*/*"));
        loadFonts();
    }

    private void loadFonts() {
        executor.execute(() -> {
            List<FontAssetModel> fonts = manager.getByProject(projectId);
            mainHandler.post(() -> adapter.setItems(fonts));
        });
    }

    private void importFont(Uri uri) {
        executor.execute(() -> {
            String name = uri.getLastPathSegment();
            FontAssetModel font = manager.importFont(projectId, uri,
                    name == null ? "font" : name);
            mainHandler.post(() -> {
                if (font == null) {
                    Toast.makeText(this, R.string.asset_font_failed,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(this, R.string.asset_font_imported,
                        Toast.LENGTH_SHORT).show();
                loadFonts();
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

    private class FontAdapter
            extends RecyclerView.Adapter<FontAdapter.Holder> {

        private final List<FontAssetModel> items = new ArrayList<>();

        void setItems(List<FontAssetModel> fonts) {
            items.clear();
            items.addAll(fonts);
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
            FontAssetModel font = items.get(position);
            holder.name.setText(font.getName());
            holder.detail.setText(font.getFamily() + " · "
                    + font.getFileName());
            holder.itemView.setOnLongClickListener(v -> {
                executor.execute(() -> {
                    manager.delete(font.getId());
                    mainHandler.post(FontManagerActivity.this::loadFonts);
                });
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
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
