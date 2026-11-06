package com.nobi.appbuilder.ui.assets;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.assets.AssetManager;
import com.nobi.appbuilder.assets.AssetMetadata;
import com.nobi.appbuilder.assets.AssetModel;
import com.nobi.appbuilder.assets.image.ImageImportManager;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asset library screen: folders, search, grid, SAF import,
 * details, rename, delete (blocked while referenced), image edit.
 */
public class AssetManagerActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private String projectId;
    private DatabaseHelper dbHelper;
    private AssetManager manager;
    private AssetLibraryController libraryController;
    private AssetFolderController folderController;
    private AssetDetailsController detailsController;
    private AssetImportController importController;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_manager);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.asset_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Toolbar toolbar = findViewById(R.id.toolbar_assets);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.asset_title);
        }
        dbHelper = new DatabaseHelper(this);
        manager = new AssetManager(this, dbHelper);
        detailsController = new AssetDetailsController(this,
                new AssetDetailsController.DetailsListener() {
                    @Override
                    public void onRename(AssetModel asset,
                                         String newResourceName) {
                        renameAsset(asset, newResourceName);
                    }

                    @Override
                    public void onDelete(AssetModel asset) {
                        deleteAsset(asset);
                    }

                    @Override
                    public void onEditImage(AssetModel asset) {
                        Intent intent = new Intent(AssetManagerActivity.this,
                                ImageEditorActivity.class);
                        intent.putExtra(ImageEditorActivity.EXTRA_PROJECT_ID,
                                projectId);
                        intent.putExtra(ImageEditorActivity.EXTRA_ASSET_ID,
                                asset.getId());
                        startActivity(intent);
                    }
                });
        libraryController = new AssetLibraryController(
                findViewById(R.id.layout_asset_root),
                new AssetLibraryController.LibraryListener() {
                    @Override
                    public void onOpen(AssetModel asset) {
                        showDetails(asset);
                    }

                    @Override
                    public void onDelete(AssetModel asset) {
                        deleteAsset(asset);
                    }
                });
        folderController = new AssetFolderController(
                findViewById(R.id.layout_asset_root),
                (folderId, folderName) -> {
                    folderController.setSelected(folderId);
                    libraryController.setFolder(folderId);
                });
        importController = new AssetImportController(this,
                (uris, names) -> importFiles(uris, names));
        Button importButton = findViewById(R.id.btn_asset_import);
        importButton.setOnClickListener(v -> importController.pickImages());
        Button newFolder = findViewById(R.id.btn_asset_new_folder);
        newFolder.setOnClickListener(v -> showNewFolderDialog());
        loadAll();
    }

    private void loadAll() {
        executor.execute(() -> {
            List<AssetModel> assets = manager.getAssets(projectId);
            List<String[]> folders = manager.getFolders(projectId);
            mainHandler.post(() -> {
                libraryController.setAssets(assets);
                List<String[]> withAll = new ArrayList<>();
                withAll.add(new String[]{null, getString(R.string.asset_all)});
                withAll.addAll(folders);
                folderController.setFolders(withAll);
            });
        });
    }

    private void importFiles(List<Uri> uris, List<String> names) {
        executor.execute(() -> {
            ImageImportManager importer = new ImageImportManager(this, dbHelper);
            ImageImportManager.BatchResult result =
                    importer.importImages(projectId, uris, names);
            mainHandler.post(() -> {
                if (!result.errors.isEmpty()) {
                    Toast.makeText(this, result.errors.get(0),
                            Toast.LENGTH_LONG).show();
                } else if (result.duplicates > 0) {
                    Toast.makeText(this,
                            getString(R.string.asset_import_duplicates,
                                    result.duplicates),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this,
                            getString(R.string.asset_imported,
                                    result.imported.size()),
                            Toast.LENGTH_SHORT).show();
                }
                loadAll();
            });
        });
    }

    private void showDetails(AssetModel asset) {
        executor.execute(() -> {
            AssetMetadata metadata =
                    manager.getRepository().getMetadata(asset.getId());
            List<String> usages =
                    manager.getReferences().findUsages(projectId, asset.getId());
            mainHandler.post(() ->
                    detailsController.show(asset, metadata, usages));
        });
    }

    private void renameAsset(AssetModel asset, String newName) {
        if (newName == null || newName.isEmpty()) {
            Toast.makeText(this, R.string.asset_name_required,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String sanitized = com.nobi.appbuilder.assets.resources
                .ResourceNamingManager.sanitize(newName);
        executor.execute(() -> {
            int updated = manager.renameAsset(projectId, asset.getId(),
                    sanitized);
            mainHandler.post(() -> {
                Toast.makeText(this,
                        getString(R.string.asset_renamed, updated),
                        Toast.LENGTH_SHORT).show();
                loadAll();
            });
        });
    }

    private void deleteAsset(AssetModel asset) {
        executor.execute(() -> {
            String error = manager.deleteAsset(projectId, asset.getId());
            mainHandler.post(() -> {
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(this, R.string.asset_deleted,
                        Toast.LENGTH_SHORT).show();
                loadAll();
            });
        });
    }

    private void showNewFolderDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.asset_folder_name_hint);
        new AlertDialog.Builder(this)
                .setTitle(R.string.asset_new_folder)
                .setView(input)
                .setPositiveButton(R.string.asset_create, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (name.isEmpty()) {
                        return;
                    }
                    executor.execute(() -> {
                        manager.createFolder(projectId, name, null);
                        mainHandler.post(this::loadAll);
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAll();
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
