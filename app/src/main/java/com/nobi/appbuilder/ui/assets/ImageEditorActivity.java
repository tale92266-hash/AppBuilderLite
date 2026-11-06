package com.nobi.appbuilder.ui.assets;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.assets.AssetModel;
import com.nobi.appbuilder.assets.AssetPreviewManager;
import com.nobi.appbuilder.assets.AssetRepository;
import com.nobi.appbuilder.assets.image.ImageCropManager;
import com.nobi.appbuilder.assets.image.ImageFormatConverter;
import com.nobi.appbuilder.assets.image.ImageOptimizationManager;
import com.nobi.appbuilder.assets.image.ImageResizer;
import com.nobi.appbuilder.assets.resources.ResourceNamingManager;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.utils.IDGenerator;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Image editor: resize, center-crop, convert to WebP, optimize.
 * Every operation writes a NEW asset file and registers a new
 * asset row — the original is never overwritten.
 */
public class ImageEditorActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_ASSET_ID = "asset_id";

    private String projectId;
    private String assetId;
    private DatabaseHelper dbHelper;
    private AssetRepository repository;
    private AssetModel asset;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        assetId = getIntent().getStringExtra(EXTRA_ASSET_ID);
        if (projectId == null || assetId == null) {
            Toast.makeText(this, R.string.asset_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_image_editor);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.asset_image_editor_title);
        }
        dbHelper = new DatabaseHelper(this);
        repository = new AssetRepository(dbHelper);
        Button resize = findViewById(R.id.btn_image_resize);
        resize.setOnClickListener(v -> resize());
        Button crop = findViewById(R.id.btn_image_crop);
        crop.setOnClickListener(v -> crop());
        Button webp = findViewById(R.id.btn_image_webp);
        webp.setOnClickListener(v -> convertWebP());
        Button optimize = findViewById(R.id.btn_image_optimize);
        optimize.setOnClickListener(v -> optimize());
        loadAsset();
    }

    private void loadAsset() {
        executor.execute(() -> {
            AssetModel loaded = repository.getById(assetId);
            mainHandler.post(() -> {
                asset = loaded;
                if (asset == null) {
                    Toast.makeText(this, R.string.asset_not_found,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                TextView info = findViewById(R.id.text_image_info);
                info.setText(AssetPreviewManager.previewDescription(asset,
                        repository.getMetadata(asset.getId())));
                ImageView preview = findViewById(R.id.image_editor_preview);
                Bitmap bitmap = AssetPreviewManager.previewBitmap(
                        new File(asset.getPath()), 512);
                if (bitmap != null) {
                    preview.setImageBitmap(bitmap);
                }
            });
        });
    }

    private int intOf(EditText input, int fallback) {
        try {
            return Integer.parseInt(input.getText().toString().trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private File outputFile(String suffix, String extension) {
        File source = new File(asset.getPath());
        String base = ResourceNamingManager.sanitize(asset.getResourceName());
        return new File(source.getParentFile(), base + suffix + "." + extension);
    }

    private void registerOutput(File file, String suffix) {
        AssetModel created = new AssetModel();
        created.setId(IDGenerator.newId());
        created.setProjectId(projectId);
        created.setName(asset.getName().replaceFirst("\\.[^.]+$",
                "") + suffix + "." + ResourceNamingManager.extensionOf(file.getName()));
        created.setType(asset.getType());
        created.setPath(file.getAbsolutePath());
        created.setSizeBytes(file.length());
        created.setCreatedAt(System.currentTimeMillis());
        created.setResourceName(ResourceNamingManager.sanitize(
                created.getName()));
        repository.insert(created);
        Toast.makeText(this, R.string.asset_image_saved,
                Toast.LENGTH_SHORT).show();
    }

    private void resize() {
        if (asset == null) {
            return;
        }
        int width = intOf(findViewById(R.id.input_image_width), 0);
        int height = intOf(findViewById(R.id.input_image_height), 0);
        if (width <= 0 || height <= 0) {
            Toast.makeText(this, R.string.asset_image_size_required,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        executor.execute(() -> {
            File target = outputFile("-resized",
                    ResourceNamingManager.extensionOf(asset.getName()));
            boolean ok = ImageResizer.resizeTo(new File(asset.getPath()),
                    target, width, height);
            mainHandler.post(() -> {
                if (!ok) {
                    Toast.makeText(this, R.string.asset_image_failed,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                registerOutput(target, "-resized");
            });
        });
    }

    private void crop() {
        if (asset == null) {
            return;
        }
        executor.execute(() -> {
            File target = outputFile("-cropped",
                    ResourceNamingManager.extensionOf(asset.getName()));
            boolean ok = ImageCropManager.centerCrop(new File(asset.getPath()),
                    target, 1, 1);
            mainHandler.post(() -> {
                if (!ok) {
                    Toast.makeText(this, R.string.asset_image_failed,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                registerOutput(target, "-cropped");
            });
        });
    }

    private void convertWebP() {
        if (asset == null) {
            return;
        }
        executor.execute(() -> {
            File target = outputFile("-webp", "webp");
            String error = ImageFormatConverter.convertChecked(
                    new File(asset.getPath()), target,
                    android.graphics.Bitmap.CompressFormat.WEBP);
            mainHandler.post(() -> {
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    return;
                }
                registerOutput(target, "-webp");
            });
        });
    }

    private void optimize() {
        if (asset == null) {
            return;
        }
        executor.execute(() -> {
            File target = ImageOptimizationManager.optimizedPath(
                    new File(asset.getPath()));
            boolean ok = ImageOptimizationManager.optimize(
                    new File(asset.getPath()), target, 1024, 85);
            mainHandler.post(() -> {
                if (!ok) {
                    Toast.makeText(this, R.string.asset_image_failed,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                registerOutput(target, "-optimized");
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
