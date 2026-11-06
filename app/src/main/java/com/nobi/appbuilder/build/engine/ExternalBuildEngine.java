package com.nobi.appbuilder.build.engine;

import android.content.Context;

import com.nobi.appbuilder.build.BuildCancellationManager;
import com.nobi.appbuilder.build.BuildEnvironmentInfo;
import com.nobi.appbuilder.build.BuildRequest;
import com.nobi.appbuilder.build.BuildResult;
import com.nobi.appbuilder.build.BuildStatus;
import com.nobi.appbuilder.build.BuildTask;
import com.nobi.appbuilder.build.logs.BuildLogManager;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.export.PackagingResult;
import com.nobi.appbuilder.export.ProjectPackagingConfig;
import com.nobi.appbuilder.export.ProjectPackagingManager;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.project.ProjectSettingsManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The honest path for devices without a local toolchain: export the
 * Android Studio project ZIP (Phase 8) and hand the user clear
 * Android Studio build instructions. The result is
 * EXTERNAL_BUILD_REQUIRED — never SUCCESS, because no APK was
 * compiled here.
 */
public class ExternalBuildEngine implements BuildEngine {

    private final Context appContext;

    public ExternalBuildEngine(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public boolean supports(BuildEnvironmentInfo environment) {
        return true;
    }

    @Override
    public BuildResult execute(BuildRequest request,
                               BuildLogManager logManager,
                               BuildCancellationManager cancellation,
                               EngineListener listener) {
        BuildResult result = new BuildResult(request.getBuildId(),
                request.getProjectId(), request.getConfiguration());
        result.setStatus(BuildStatus.RUNNING);
        listener.onTask(BuildTask.PREPARE_WORKSPACE);
        DatabaseHelper dbHelper = new DatabaseHelper(appContext);
        ProjectPackagingConfig config;
        try {
            ProjectRepository projects = new ProjectRepository(dbHelper);
            ProjectModel project = projects.getById(request.getProjectId());
            ProjectSettingsManager settings = new ProjectSettingsManager(
                    dbHelper, projects);
            config = ProjectPackagingConfig.defaultsFor(project, settings);
            config.setPackageName(request.getConfiguration()
                    .getApplicationId());
            config.setMinSdk(request.getConfiguration().getMinSdk());
            config.setTargetSdk(request.getConfiguration().getTargetSdk());
            config.setVersionName(request.getConfiguration().getVersionName());
            config.setVersionCode(request.getConfiguration().getVersionCode());
        } finally {
            dbHelper.close();
        }
        logManager.appendLine("No local build tools on this device.");
        logManager.appendLine("Exporting the Android Studio project ZIP "
                + "for an external build.");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<PackagingResult> success = new AtomicReference<>();
        AtomicReference<String> failure = new AtomicReference<>();
        ProjectPackagingManager manager = new ProjectPackagingManager(
                appContext, request.getProjectId(), config,
                new ProjectPackagingManager.Listener() {
                    @Override
                    public void onStage(int index, String label) {
                        logManager.appendLine(label);
                        listener.onProgress(index * 10);
                    }

                    @Override
                    public void onProgress(int percent) {
                        listener.onProgress(percent);
                    }

                    @Override
                    public void onFinished(PackagingResult packagingResult) {
                        success.set(packagingResult);
                        latch.countDown();
                    }

                    @Override
                    public void onFailed(String message) {
                        failure.set(message);
                        latch.countDown();
                    }
                });
        manager.start();
        try {
            latch.await(10, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        manager.release();
        if (success.get() != null) {
            PackagingResult packaging = success.get();
            String zipPath = packaging.getZipFile() != null
                    ? packaging.getZipFile().getAbsolutePath()
                    : packaging.getOutputUri();
            result.setExternalZipPath(zipPath);
            logManager.appendLine("Project exported to " + zipPath);
            logManager.appendLine("Open it in Android Studio and run:");
            logManager.appendLine("  ./gradlew "
                    + request.getConfiguration().getGradleTask());
            result.setStatus(BuildStatus.EXTERNAL_BUILD_REQUIRED);
            return result;
        }
        logManager.appendLine("Export failed: "
                + (failure.get() == null ? "unknown error" : failure.get()));
        result.setStatus(BuildStatus.FAILED);
        return result;
    }
}
