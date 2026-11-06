package com.nobi.appbuilder.build;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.nobi.appbuilder.build.diagnostics.BuildDiagnosticsManager;
import com.nobi.appbuilder.build.diagnostics.BuildReportGenerator;
import com.nobi.appbuilder.build.engine.BuildEngine;
import com.nobi.appbuilder.build.engine.ExternalBuildEngine;
import com.nobi.appbuilder.build.engine.LocalGradleBuildEngine;
import com.nobi.appbuilder.build.logs.BuildLogExporter;
import com.nobi.appbuilder.build.logs.BuildLogManager;
import com.nobi.appbuilder.build.logs.BuildLogRepository;
import com.nobi.appbuilder.build.logs.BuildLogEntry;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Owns the build lifecycle: validate → detect environment → pick the
 * honest engine → run → diagnose → save history and report. One
 * build runs at a time.
 */
public class BuildManager {

    public interface BuildListener {
        void onTask(BuildTask task);

        void onLog(BuildLogEntry entry);

        void onProgress(int percent);

        void onFinished(BuildResult result);

        void onFailed(String message);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Context appContext;
    private final BuildQueueManager queue = new BuildQueueManager();
    private final BuildCancellationManager cancellation =
            new BuildCancellationManager();

    public BuildManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /** Starts a build; returns the build id, or null when invalid. */
    public String startBuild(String projectId, String projectName,
                             BuildConfiguration config,
                             BuildListener listener) {
        BuildValidator validator = new BuildValidator(appContext);
        if (!validator.validateConfiguration(config).isEmpty()) {
            mainHandler.post(() -> listener.onFailed(
                    String.join("; ", validator.validateConfiguration(config))));
            return null;
        }
        String buildId = UUID.randomUUID().toString();
        BuildRequest request = new BuildRequest(buildId, projectId,
                projectName, config);
        if (!queue.enqueue(request)) {
            mainHandler.post(() -> listener.onFailed(
                    "Too many builds are queued — wait for one to finish"));
            return null;
        }
        cancellation.register(buildId);
        executor.execute(() -> runBuild(request, listener));
        return buildId;
    }

    public void cancelBuild(String buildId) {
        cancellation.cancel(buildId);
        queue.cancelQueued(buildId);
    }

    public boolean isBusy() {
        return queue.isBusy();
    }

    private void runBuild(BuildRequest request, BuildListener listener) {
        BuildRequest active = queue.startNext();
        if (active == null || !active.getBuildId().equals(request.getBuildId())) {
            return;
        }
        BuildLogManager logManager = new BuildLogManager();
        logManager.addListener(entry ->
                mainHandler.post(() -> listener.onLog(entry)));
        BuildResult result = null;
        BuildEnvironmentInfo environment = null;
        try {
            mainHandler.post(() -> listener.onTask(BuildTask.CHECK_ENVIRONMENT));
            environment = new BuildEnvironmentDetector(appContext)
                    .detect(request.getConfiguration().getTargetSdk());
            logManager.appendLine("Environment: " + environment.getSummary());
            mainHandler.post(() -> listener.onTask(BuildTask.VALIDATE_PROJECT));
            BuildEngine engine;
            if (environment.getCapability()
                    == BuildCapability.LOCAL_BUILD_SUPPORTED) {
                engine = new LocalGradleBuildEngine(appContext);
            } else {
                engine = new ExternalBuildEngine(appContext);
            }
            final BuildEnvironmentInfo detected = environment;
            result = engine.execute(request, logManager, cancellation,
                    new BuildEngine.EngineListener() {
                        @Override
                        public void onTask(BuildTask task) {
                            mainHandler.post(() -> listener.onTask(task));
                        }

                        @Override
                        public void onProgress(int percent) {
                            mainHandler.post(() -> listener.onProgress(percent));
                        }
                    });
            if (cancellation.isCancelled(request.getBuildId())
                    && result.getStatus() != BuildStatus.CANCELLED) {
                result.setStatus(BuildStatus.CANCELLED);
            }
            if (result.getStatus() == BuildStatus.FAILED
                    && result.getErrors().isEmpty()) {
                BuildDiagnosticsManager diagnostics =
                        new BuildDiagnosticsManager();
                diagnostics.analyse(logManager.getEntries());
                result.getErrors().addAll(diagnostics.getErrors());
                result.getWarnings().addAll(diagnostics.getWarnings());
            }
            if (result.getStatus() == BuildStatus.SUCCESS
                    && result.getWarnings().isEmpty()) {
                BuildDiagnosticsManager diagnostics =
                        new BuildDiagnosticsManager();
                diagnostics.analyse(logManager.getEntries());
                result.getWarnings().addAll(diagnostics.getWarnings());
            }
            if (request.getConfiguration().isIncludeReport()) {
                result.setReportText(BuildReportGenerator.generate(result,
                        detected, request.getProjectName()));
            }
            if (request.getConfiguration().isPreserveLogs()) {
                File logFile = new BuildLogExporter(appContext)
                        .export(request.getBuildId(), logManager);
                if (logFile != null) {
                    result.setLogPath(logFile.getAbsolutePath());
                }
            }
            DatabaseHelper dbHelper = new DatabaseHelper(appContext);
            try {
                new BuildLogRepository(dbHelper)
                        .insert(result, request.getProjectName());
            } finally {
                dbHelper.close();
            }
            final BuildResult finished = result;
            mainHandler.post(() -> listener.onFinished(finished));
        } catch (Exception e) {
            final String message = e.getMessage() == null
                    ? "Build failed" : e.getMessage();
            mainHandler.post(() -> listener.onFailed(message));
        } finally {
            cancellation.unregister(request.getBuildId());
            queue.finishActive();
        }
    }

    public void release() {
        executor.shutdownNow();
    }
}
