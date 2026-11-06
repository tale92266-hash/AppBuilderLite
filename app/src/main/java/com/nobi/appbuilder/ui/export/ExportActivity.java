package com.nobi.appbuilder.ui.export;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.nobi.appbuilder.R;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.export.PackagingResult;
import com.nobi.appbuilder.export.ProjectPackagingConfig;
import com.nobi.appbuilder.export.ProjectPackagingManager;
import com.nobi.appbuilder.export.storage.ExportHistoryRepository;
import com.nobi.appbuilder.export.storage.ExportStorageManager;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.project.ProjectSettingsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Export screen: configuration form, progress, result, history.
 * Runs the export off the main thread and never allows two exports
 * for the same project to run at once.
 */
public class ExportActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private String projectId;
    private DatabaseHelper dbHelper;
    private ProjectPackagingConfig baseConfig;
    private ExportConfigurationController configController;
    private ExportProgressController progressController;
    private ExportResultController resultController;
    private ExportHistoryAdapter historyAdapter;
    private ExportFileBrowserController fileBrowser;
    private ProjectPackagingManager runningManager;
    private PackagingResult lastResult;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ActivityResultLauncher<Uri> treePicker =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocumentTree(), uri -> {
                        if (uri == null) {
                            return;
                        }
                        try {
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        } catch (Exception ignored) {
                        }
                        new ExportStorageManager(this)
                                .setCustomOutputTreeUri(uri.toString());
                        baseConfig.setOutputTreeUri(uri.toString());
                        configController.setOutputDirLabel(uri.toString());
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.export_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        MaterialToolbar toolbar = findViewById(R.id.toolbar_export);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);
        fileBrowser = new ExportFileBrowserController(this);
        configController = new ExportConfigurationController(
                findViewById(R.id.layout_export_configuration),
                () -> treePicker.launch(null));
        progressController = new ExportProgressController(
                findViewById(R.id.layout_export_progress));
        resultController = new ExportResultController(findViewById(
                R.id.layout_export_root),
                new ExportResultController.ResultListener() {
                    @Override
                    public void onOpenLocation() {
                        openLastResult();
                    }

                    @Override
                    public void onShare() {
                        shareLastResult();
                    }

                    @Override
                    public void onExportAnother() {
                        showConfiguration();
                    }

                    @Override
                    public void onViewReport() {
                        if (lastResult != null) {
                            new AlertDialog.Builder(ExportActivity.this)
                                    .setTitle(R.string.export_report)
                                    .setMessage(lastResult.getReport().toText())
                                    .setPositiveButton(android.R.string.ok,
                                            null)
                                    .show();
                        }
                    }
                });

        RecyclerView historyView = findViewById(R.id.recycler_export_history);
        historyView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new ExportHistoryAdapter(
                new ExportHistoryAdapter.OnHistoryActionListener() {
                    @Override
                    public void onOpen(ExportHistoryRepository.ExportRecord r) {
                        fileBrowser.open(r);
                    }

                    @Override
                    public void onShare(ExportHistoryRepository.ExportRecord r) {
                        fileBrowser.share(r);
                    }

                    @Override
                    public void onDelete(ExportHistoryRepository.ExportRecord r) {
                        showHistoryOptions(r);
                    }
                });
        historyView.setAdapter(historyAdapter);

        findViewById(R.id.btn_export_start)
                .setOnClickListener(v -> startExport());
        loadDefaults();
        refreshHistory();
    }

    private void loadDefaults() {
        executor.execute(() -> {
            ProjectRepository projects = new ProjectRepository(dbHelper);
            ProjectModel project = projects.getById(projectId);
            ProjectSettingsManager settings = new ProjectSettingsManager(
                    dbHelper, projects);
            ProjectPackagingConfig defaults =
                    ProjectPackagingConfig.defaultsFor(project, settings);
            String treeUri = new ExportStorageManager(this)
                    .getCustomOutputTreeUri();
            defaults.setOutputTreeUri(treeUri);
            runOnUiThread(() -> {
                baseConfig = defaults;
                String label = treeUri != null ? treeUri
                        : getString(R.string.export_default_dir);
                configController.bind(defaults, label);
            });
        });
    }

    private void refreshHistory() {
        executor.execute(() -> {
            List<ExportHistoryRepository.ExportRecord> records =
                    new ExportHistoryRepository(dbHelper)
                            .getByProject(projectId);
            runOnUiThread(() -> historyAdapter.setRecords(records));
        });
    }

    private void startExport() {
        if (runningManager != null || baseConfig == null) {
            return;
        }
        List<String> errors = new ArrayList<>();
        ProjectPackagingConfig config =
                configController.readConfig(baseConfig, errors);
        if (config == null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.export_invalid_config)
                    .setMessage(String.join("\n", errors))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }
        baseConfig = config;
        resultController.hide();
        progressController.show();
        runningManager = new ProjectPackagingManager(this, projectId, config,
                new ProjectPackagingManager.Listener() {
                    @Override
                    public void onStage(int index, String label) {
                        progressController.setStage(index);
                    }

                    @Override
                    public void onProgress(int percent) {
                        progressController.setProgress(percent);
                    }

                    @Override
                    public void onFinished(PackagingResult result) {
                        runningManager = null;
                        lastResult = result;
                        progressController.hide();
                        resultController.showSuccess(result);
                        refreshHistory();
                    }

                    @Override
                    public void onFailed(String message) {
                        runningManager = null;
                        progressController.hide();
                        resultController.showError(message);
                        refreshHistory();
                    }
                });
        runningManager.start();
    }

    private void showConfiguration() {
        resultController.hide();
        progressController.hide();
    }

    private void openLastResult() {
        if (lastResult == null || lastResult.getZipFile() == null) {
            if (lastResult != null && lastResult.getOutputUri() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(lastResult.getOutputUri()),
                        "application/zip");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            }
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(
                androidx.core.content.FileProvider.getUriForFile(this,
                        "com.nobi.appbuilder.fileprovider",
                        lastResult.getZipFile()),
                "application/zip");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void shareLastResult() {
        if (lastResult == null) {
            return;
        }
        Uri uri;
        if (lastResult.getZipFile() != null) {
            uri = androidx.core.content.FileProvider.getUriForFile(this,
                    "com.nobi.appbuilder.fileprovider",
                    lastResult.getZipFile());
        } else if (lastResult.getOutputUri() != null) {
            uri = Uri.parse(lastResult.getOutputUri());
        } else {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent,
                getString(R.string.export_share)));
    }

    private void showHistoryOptions(
            ExportHistoryRepository.ExportRecord record) {
        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_export_options, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        dialogView.findViewById(R.id.btn_export_options_open)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    fileBrowser.open(record);
                });
        dialogView.findViewById(R.id.btn_export_options_share)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    fileBrowser.share(record);
                });
        dialogView.findViewById(R.id.btn_export_options_delete)
                .setOnClickListener(v -> {
                    dialog.dismiss();
                    new ExportHistoryRepository(dbHelper).delete(record.id);
                    refreshHistory();
                });
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (runningManager != null) {
            runningManager.cancel();
            runningManager.release();
        }
        executor.shutdownNow();
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
