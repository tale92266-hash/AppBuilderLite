package com.nobi.appbuilder.ui.build;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.nobi.appbuilder.R;
import com.nobi.appbuilder.build.BuildConfiguration;
import com.nobi.appbuilder.build.BuildEnvironmentDetector;
import com.nobi.appbuilder.build.BuildEnvironmentInfo;
import com.nobi.appbuilder.build.BuildManager;
import com.nobi.appbuilder.build.BuildResult;
import com.nobi.appbuilder.build.BuildStatus;
import com.nobi.appbuilder.build.BuildTask;
import com.nobi.appbuilder.build.cloud.CloudBuildSettings;
import com.nobi.appbuilder.build.cloud.GitHubClient;
import com.nobi.appbuilder.build.apk.ApkInstaller;
import com.nobi.appbuilder.build.apk.ApkManager;
import com.nobi.appbuilder.build.apk.ApkRepository;
import com.nobi.appbuilder.build.apk.ApkShareManager;
import com.nobi.appbuilder.build.logs.BuildLogEntry;
import com.nobi.appbuilder.build.logs.BuildLogRepository;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.project.ProjectSettingsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Build screen: environment summary, configuration, progress, live
 * logs, diagnostics, result with APK actions, APK list and history.
 * One build at a time; cancellation is supported.
 */
public class BuildActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private String projectId;
    private String projectName;
    private DatabaseHelper dbHelper;
    private BuildManager buildManager;
    private BuildConfiguration baseConfig;
    private BuildConfigurationController configController;
    private BuildProgressController progressController;
    private BuildLogController logController;
    private BuildDiagnosticsController diagnosticsController;
    private BuildResultController resultController;
    private ApkListAdapter apkAdapter;
    private BuildHistoryAdapter historyAdapter;
    private BuildResult lastResult;
    private String runningBuildId;
    private CloudBuildSettings cloudSettings;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    // APK the user asked to install while the "install unknown apps"
    // permission was missing. When they come back from Settings with
    // the permission granted, onResume fires the installer itself —
    // the user must never have to tap Install a second time, and a
    // tap must never end in silence on the same screen.
    private File pendingInstallFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.build_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        MaterialToolbar toolbar = findViewById(R.id.toolbar_build);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DatabaseHelper(this);
        buildManager = new BuildManager(this);
        cloudSettings = new CloudBuildSettings(this);
        configController = new BuildConfigurationController(
                findViewById(R.id.layout_build_configuration));
        configController.setModeChangeListener(mode -> updateCloudRow());
        findViewById(R.id.btn_build_cloud_connect)
                .setOnClickListener(v -> onCloudConnectButton());
        progressController = new BuildProgressController(
                findViewById(R.id.layout_build_progress));
        logController = new BuildLogController(
                findViewById(R.id.layout_build_logs),
                new BuildLogController.LogActionListener() {
                    @Override
                    public void onCopyLogs() {
                        copyLogs();
                    }

                    @Override
                    public void onExportLogs() {
                        exportLogs();
                    }
                });
        diagnosticsController = new BuildDiagnosticsController(
                findViewById(R.id.layout_build_diagnostics));
        resultController = new BuildResultController(
                findViewById(R.id.layout_build_root),
                new BuildResultController.ResultListener() {
                    @Override
                    public void onInstallApk() {
                        installLastApk();
                    }

                    @Override
                    public void onShareApk() {
                        shareLastApk();
                    }

                    @Override
                    public void onOpenApkLocation() {
                        openLastApkLocation();
                    }

                    @Override
                    public void onDeleteApk() {
                        deleteLastApk();
                    }

                    @Override
                    public void onViewReport() {
                        if (lastResult != null
                                && lastResult.getReportText() != null) {
                            new AlertDialog.Builder(BuildActivity.this)
                                    .setTitle(R.string.build_report)
                                    .setMessage(lastResult.getReportText())
                                    .setPositiveButton(android.R.string.ok,
                                            null)
                                    .show();
                        }
                    }

                    @Override
                    public void onBuildAnother() {
                        resultController.hide();
                        diagnosticsController.hide();
                    }
                });

        RecyclerView apkView = findViewById(R.id.recycler_build_apks);
        apkView.setLayoutManager(new LinearLayoutManager(this));
        apkAdapter = new ApkListAdapter(
                new ApkListAdapter.OnApkActionListener() {
                    @Override
                    public void onInstall(ApkRepository.ApkRecord record) {
                        installApk(record.file);
                    }

                    @Override
                    public void onShare(ApkRepository.ApkRecord record) {
                        new ApkShareManager(BuildActivity.this)
                                .share(record.file);
                    }

                    @Override
                    public void onDelete(ApkRepository.ApkRecord record) {
                        confirmDeleteApk(record.file);
                    }
                });
        apkView.setAdapter(apkAdapter);

        RecyclerView historyView =
                findViewById(R.id.recycler_build_history);
        historyView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new BuildHistoryAdapter(
                new BuildHistoryAdapter.OnBuildHistoryActionListener() {
                    @Override
                    public void onOpen(BuildLogRepository.BuildRecord record) {
                        openLogFile(record.logPath);
                    }

                    @Override
                    public void onDelete(BuildLogRepository.BuildRecord record) {
                        new BuildLogRepository(dbHelper).delete(record.id);
                        refreshHistory();
                    }
                });
        historyView.setAdapter(historyAdapter);

        findViewById(R.id.btn_build_start)
                .setOnClickListener(v -> startBuild());
        findViewById(R.id.btn_build_cancel)
                .setOnClickListener(v -> cancelBuild());

        loadDefaults();
        detectEnvironment();
        updateCloudRow();
        refreshApks();
        refreshHistory();
    }

    /** Cloud mode row: status text + Connect/Disconnect button. */
    private void updateCloudRow() {
        Button connect = findViewById(R.id.btn_build_cloud_connect);
        if (!BuildConfiguration.MODE_CLOUD_APK.equals(
                configController.getSelectedBuildMode())) {
            configController.setCloudStatusText(null);
            connect.setVisibility(View.GONE);
            return;
        }
        if (cloudSettings.isConnected()) {
            configController.setCloudStatusText(getString(
                    R.string.build_cloud_status_connected,
                    cloudSettings.getOwner()));
            connect.setText(R.string.build_cloud_disconnect);
        } else {
            configController.setCloudStatusText(getString(
                    R.string.build_cloud_status_not_connected));
            connect.setText(R.string.build_cloud_connect);
        }
        connect.setVisibility(View.VISIBLE);
    }

    private void onCloudConnectButton() {
        if (cloudSettings.isConnected()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.build_cloud_disconnect)
                    .setMessage(getString(
                            R.string.build_cloud_status_connected,
                            cloudSettings.getOwner()))
                    .setPositiveButton(R.string.build_cloud_disconnect,
                            (d, w) -> {
                                cloudSettings.disconnect();
                                updateCloudRow();
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return;
        }
        showCloudConnectDialog();
    }

    private void showCloudConnectDialog() {
        EditText tokenInput = new EditText(this);
        tokenInput.setHint(R.string.build_cloud_token_hint);
        tokenInput.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        tokenInput.setPadding(pad, pad, pad, pad);
        new AlertDialog.Builder(this)
                .setTitle(R.string.build_cloud_connect_title)
                .setMessage(R.string.build_cloud_connect_message)
                .setView(tokenInput)
                .setPositiveButton(R.string.build_cloud_connect,
                        (d, w) -> {
                            String token = tokenInput.getText() == null ? ""
                                    : tokenInput.getText().toString().trim();
                            if (token.isEmpty()) {
                                Toast.makeText(this,
                                        R.string.build_cloud_connect_failed,
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            verifyCloudToken(token);
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /** Checks the token against GitHub before saving it. */
    private void verifyCloudToken(String token) {
        executor.execute(() -> {
            try {
                String login = new GitHubClient(token).getUserLogin();
                runOnUiThread(() -> {
                    cloudSettings.connect(token, login);
                    updateCloudRow();
                    Toast.makeText(this, getString(
                            R.string.build_cloud_connected, login),
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this,
                        R.string.build_cloud_connect_failed,
                        Toast.LENGTH_LONG).show());
            }
        });
    }

    private void loadDefaults() {
        executor.execute(() -> {
            ProjectRepository projects = new ProjectRepository(dbHelper);
            ProjectModel project = projects.getById(projectId);
            ProjectSettingsManager settings = new ProjectSettingsManager(
                    dbHelper, projects);
            BuildConfiguration config = new BuildConfiguration();
            if (project != null) {
                projectName = project.getAppName();
                config.setApplicationId(project.getPackageName());
                config.setMinSdk(project.getMinSdk());
            }
            config.setTargetSdk(parseInt(settings.getSetting(projectId,
                    ProjectSettingsManager.KEY_TARGET_SDK, "35"), 35));
            config.setVersionName(settings.getSetting(projectId,
                    ProjectSettingsManager.KEY_VERSION_NAME, "1.0"));
            config.setVersionCode(parseInt(settings.getSetting(projectId,
                    ProjectSettingsManager.KEY_VERSION_CODE, "1"), 1));
            runOnUiThread(() -> {
                baseConfig = config;
                configController.bind(config);
            });
        });
    }

    private void detectEnvironment() {
        executor.execute(() -> {
            BuildEnvironmentInfo info = new BuildEnvironmentDetector(this)
                    .detect(baseConfig == null ? 35 : baseConfig.getTargetSdk());
            runOnUiThread(() -> {
                TextView env = findViewById(R.id.text_build_environment);
                env.setText(info.getSummary());
            });
        });
    }

    private void startBuild() {
        if (runningBuildId != null || baseConfig == null) {
            return;
        }
        List<String> errors = new ArrayList<>();
        BuildConfiguration config =
                configController.readConfig(baseConfig, errors);
        if (config == null) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.build_invalid_config)
                    .setMessage(String.join("\n", errors))
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return;
        }
        if (config.isCloudBuild() && !cloudSettings.isConnected()) {
            Toast.makeText(this, R.string.build_cloud_not_connected,
                    Toast.LENGTH_LONG).show();
            showCloudConnectDialog();
            return;
        }
        baseConfig = config;
        resultController.hide();
        diagnosticsController.hide();
        logController.clear();
        progressController.show();
        findViewById(R.id.btn_build_start).setEnabled(false);
        runningBuildId = buildManager.startBuild(projectId,
                projectName == null ? projectId : projectName, config,
                new BuildManager.BuildListener() {
                    @Override
                    public void onTask(BuildTask task) {
                        progressController.setTask(task);
                    }

                    @Override
                    public void onLog(BuildLogEntry entry) {
                        logController.addEntry(entry);
                        progressController.setCounts(
                                countErrors(), countWarnings());
                    }

                    @Override
                    public void onProgress(int percent) {
                        progressController.setProgress(percent, false);
                    }

                    @Override
                    public void onFinished(BuildResult result) {
                        runningBuildId = null;
                        lastResult = result;
                        findViewById(R.id.btn_build_start).setEnabled(true);
                        progressController.hide();
                        diagnosticsController.show(result.getErrors(),
                                result.getWarnings());
                        if (result.getStatus() == BuildStatus.SUCCESS) {
                            resultController.showSuccess(result);
                        } else if (result.getStatus()
                                == BuildStatus.EXTERNAL_BUILD_REQUIRED) {
                            resultController.showExternal(result);
                        } else {
                            resultController.showFailed(
                                    result.getErrors().isEmpty()
                                            ? "Build failed — see the log"
                                            : result.getErrors().get(0)
                                            .toString());
                        }
                        refreshApks();
                        refreshHistory();
                    }

                    @Override
                    public void onFailed(String message) {
                        runningBuildId = null;
                        findViewById(R.id.btn_build_start).setEnabled(true);
                        progressController.hide();
                        resultController.showFailed(message);
                        refreshHistory();
                    }
                });
    }

    private int countErrors() {
        int count = 0;
        for (BuildLogEntry entry : logController.getAllEntries()) {
            if (entry.isError()) {
                count++;
            }
        }
        return count;
    }

    private int countWarnings() {
        int count = 0;
        for (BuildLogEntry entry : logController.getAllEntries()) {
            if (entry.isWarning()) {
                count++;
            }
        }
        return count;
    }

    private void cancelBuild() {
        if (runningBuildId != null) {
            buildManager.cancelBuild(runningBuildId);
        }
    }

    private void refreshApks() {
        executor.execute(() -> {
            List<ApkRepository.ApkRecord> records =
                    new ApkRepository(this).getByProject(projectId);
            runOnUiThread(() -> apkAdapter.setRecords(records));
        });
    }

    private void refreshHistory() {
        executor.execute(() -> {
            List<BuildLogRepository.BuildRecord> records =
                    new BuildLogRepository(dbHelper).getByProject(projectId);
            runOnUiThread(() -> historyAdapter.setRecords(records));
        });
    }

    private void installLastApk() {
        if (lastResult != null && lastResult.getApkFile() != null) {
            installApk(lastResult.getApkFile());
        } else {
            Toast.makeText(this, R.string.build_no_apk,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (pendingInstallFile != null
                && new ApkInstaller(this).canRequestInstall()) {
            File apkFile = pendingInstallFile;
            pendingInstallFile = null;
            installApk(apkFile);
        }
    }

    private void installApk(File apkFile) {
        ApkInstaller installer = new ApkInstaller(this);
        if (!installer.canRequestInstall()) {
            pendingInstallFile = apkFile;
            new AlertDialog.Builder(this)
                    .setTitle(R.string.build_install_permission)
                    .setMessage(R.string.build_install_permission_text)
                    .setPositiveButton(R.string.build_open_settings,
                            (d, w) -> startActivity(
                                    installer.installPermissionIntent()))
                    .setNegativeButton(android.R.string.cancel,
                            (d, w) -> pendingInstallFile = null)
                    .show();
            return;
        }
        // Never a silent tap: the user sees this even if the system
        // installer then fails to open for any reason.
        Toast.makeText(this, R.string.build_install_opening,
                Toast.LENGTH_SHORT).show();
        if (!installer.install(apkFile)) {
            Toast.makeText(this, R.string.build_install_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void shareLastApk() {
        if (lastResult != null && lastResult.getApkFile() != null) {
            new ApkShareManager(this).share(lastResult.getApkFile());
        } else {
            Toast.makeText(this, R.string.build_no_apk,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void openLastApkLocation() {
        if (lastResult != null && lastResult.getApkFile() != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(FileProvider.getUriForFile(this,
                    "com.nobi.appbuilder.fileprovider",
                    lastResult.getApkFile()),
                    "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.build_no_apk,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void deleteLastApk() {
        if (lastResult != null && lastResult.getApkFile() != null) {
            confirmDeleteApk(lastResult.getApkFile());
        } else {
            Toast.makeText(this, R.string.build_no_apk,
                    Toast.LENGTH_LONG).show();
        }
    }

    private void confirmDeleteApk(File apkFile) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.build_delete_apk)
                .setMessage(apkFile.getName())
                .setPositiveButton(R.string.build_delete, (d, w) -> {
                    new ApkManager(this).deleteApk(apkFile);
                    refreshApks();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void openLogFile(String logPath) {
        if (logPath == null) {
            return;
        }
        File file = new File(logPath);
        if (!file.exists()) {
            Toast.makeText(this, R.string.build_log_missing,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(this,
                "com.nobi.appbuilder.fileprovider", file), "text/plain");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void copyLogs() {
        StringBuilder out = new StringBuilder();
        for (BuildLogEntry entry : logController.getAllEntries()) {
            out.append(entry.getText()).append("\n");
        }
        ClipboardManager clipboard = (ClipboardManager)
                getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Build log",
                out.toString()));
        Toast.makeText(this, R.string.build_log_copied,
                Toast.LENGTH_SHORT).show();
    }

    private void exportLogs() {
        Toast.makeText(this, R.string.build_log_exported,
                Toast.LENGTH_SHORT).show();
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    @Override
    protected void onDestroy() {
        if (runningBuildId != null) {
            buildManager.cancelBuild(runningBuildId);
        }
        buildManager.release();
        executor.shutdownNow();
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
