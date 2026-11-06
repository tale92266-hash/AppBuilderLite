package com.nobi.appbuilder.build.engine;

import android.content.Context;

import com.nobi.appbuilder.build.BuildCancellationManager;
import com.nobi.appbuilder.build.BuildCapability;
import com.nobi.appbuilder.build.BuildEnvironmentDetector;
import com.nobi.appbuilder.build.BuildEnvironmentInfo;
import com.nobi.appbuilder.build.BuildOutputManager;
import com.nobi.appbuilder.build.BuildRequest;
import com.nobi.appbuilder.build.BuildResult;
import com.nobi.appbuilder.build.BuildStatus;
import com.nobi.appbuilder.build.BuildTask;
import com.nobi.appbuilder.build.apk.ApkMetadataReader.ApkMetadata;
import com.nobi.appbuilder.build.apk.ApkValidator;
import com.nobi.appbuilder.build.diagnostics.BuildDiagnosticsManager;
import com.nobi.appbuilder.build.logs.BuildLogManager;
import com.nobi.appbuilder.build.storage.BuildStorageManager;
import com.nobi.appbuilder.build.storage.BuildWorkspaceManager;
import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.export.AndroidStudioProjectBuilder;
import com.nobi.appbuilder.export.ProjectFileManager;
import com.nobi.appbuilder.export.ProjectPackagingConfig;
import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.GenerationResult;
import com.nobi.appbuilder.generator.ProjectGenerator;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Runs a real Gradle build — only when the environment detector
 * confirms a complete compatible toolchain. The generated project
 * is materialised into an isolated workspace (Phase 7 + Phase 8
 * builders, reused), ./gradlew assembles it, and the APK is located
 * and verified. No toolchain means this engine refuses to run.
 */
public class LocalGradleBuildEngine implements BuildEngine {

    private final Context appContext;

    public LocalGradleBuildEngine(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public boolean supports(BuildEnvironmentInfo environment) {
        return environment != null
                && environment.getCapability()
                == BuildCapability.LOCAL_BUILD_SUPPORTED;
    }

    @Override
    public BuildResult execute(BuildRequest request,
                               BuildLogManager logManager,
                               BuildCancellationManager cancellation,
                               EngineListener listener) {
        BuildResult result = new BuildResult(request.getBuildId(),
                request.getProjectId(), request.getConfiguration());
        result.setStatus(BuildStatus.RUNNING);
        BuildEnvironmentInfo environment =
                new BuildEnvironmentDetector(appContext)
                        .detect(request.getConfiguration().getTargetSdk());
        if (!supports(environment)) {
            logManager.appendLine(
                    "Local build is not supported in this environment: "
                            + environment.getSummary());
            result.setStatus(BuildStatus.EXTERNAL_BUILD_REQUIRED);
            return result;
        }
        File workspace = null;
        try {
            listener.onTask(BuildTask.VALIDATE_PROJECT);
            DatabaseHelper dbHelper = new DatabaseHelper(appContext);
            IRDocument document;
            try {
                document = new DocumentRepository(dbHelper)
                        .loadDocument(request.getProjectId());
            } finally {
                dbHelper.close();
            }
            if (document == null || document.getProject() == null) {
                logManager.appendLine("Project could not be loaded");
                result.setStatus(BuildStatus.FAILED);
                return result;
            }
            listener.onTask(BuildTask.PREPARE_WORKSPACE);
            workspace = new BuildWorkspaceManager(appContext)
                    .createWorkspace();
            DatabaseHelper genDb = new DatabaseHelper(appContext);
            GenerationResult generation;
            try {
                GenerationContext context = new GenerationContext(document,
                        document.getProject(),
                        request.getConfiguration().getApplicationId(),
                        request.getConfiguration().getMinSdk(),
                        request.getConfiguration().getTargetSdk(),
                        request.getConfiguration().getVersionCode(),
                        request.getConfiguration().getVersionName(),
                        new AssetRepository(genDb));
                generation = new ProjectGenerator(context).generate();
            } finally {
                genDb.close();
            }
            if (!generation.isSuccess()) {
                logManager.appendLine("Code generation failed");
                result.setStatus(BuildStatus.FAILED);
                return result;
            }
            ProjectPackagingConfig packagingConfig =
                    new ProjectPackagingConfig();
            packagingConfig.setProjectName(request.getProjectName());
            packagingConfig.setAppName(request.getProjectName());
            packagingConfig.setPackageName(
                    request.getConfiguration().getApplicationId());
            packagingConfig.setMinSdk(request.getConfiguration().getMinSdk());
            packagingConfig.setTargetSdk(
                    request.getConfiguration().getTargetSdk());
            packagingConfig.setVersionName(
                    request.getConfiguration().getVersionName());
            packagingConfig.setVersionCode(
                    request.getConfiguration().getVersionCode());
            List<String> buildProblems = new ArrayList<>();
            new AndroidStudioProjectBuilder(appContext).build(workspace,
                    packagingConfig, generation, buildProblems);
            for (String problem : buildProblems) {
                logManager.appendLine("Packaging warning: " + problem);
            }
            new File(workspace, "gradlew").setExecutable(true);

            if (cancellation.isCancelled(request.getBuildId())) {
                result.setStatus(BuildStatus.CANCELLED);
                return result;
            }
            listener.onTask(BuildTask.COMPILE_JAVA);
            String task = GradleCommandBuilder.taskFor(
                    request.getConfiguration().isRelease());
            List<String> command = GradleCommandBuilder.commandFor(workspace,
                    task);
            Map<String, String> env = new HashMap<>();
            env.put("ANDROID_HOME", environment.getSdkPath());
            env.put("ANDROID_SDK_ROOT", environment.getSdkPath());
            String javaHome = System.getenv("JAVA_HOME");
            if (javaHome != null) {
                env.put("JAVA_HOME", javaHome);
            }
            BuildProcessManager processManager = new BuildProcessManager();
            int exit = processManager.run(workspace, command, env,
                    line -> {
                        logManager.appendLine(line);
                        if (line.startsWith("> Task :app:compile")) {
                            listener.onTask(BuildTask.COMPILE_JAVA);
                        } else if (line.contains("processDebugResources")
                                || line.contains("processReleaseResources")) {
                            listener.onTask(BuildTask.PROCESS_RESOURCES);
                        } else if (line.contains("packageDebug")
                                || line.contains("packageRelease")) {
                            listener.onTask(BuildTask.PACKAGE_APK);
                        }
                    },
                    () -> cancellation.isCancelled(request.getBuildId()));
            if (cancellation.isCancelled(request.getBuildId())
                    || exit == -1) {
                result.setStatus(BuildStatus.CANCELLED);
                return result;
            }
            if (exit != 0) {
                logManager.appendLine("Gradle exited with code " + exit);
                BuildDiagnosticsManager diagnostics =
                        new BuildDiagnosticsManager();
                diagnostics.analyse(logManager.getEntries());
                result.getErrors().addAll(diagnostics.getErrors());
                result.getWarnings().addAll(diagnostics.getWarnings());
                result.setStatus(BuildStatus.FAILED);
                return result;
            }
            listener.onTask(BuildTask.VERIFY_APK);
            File apk = new BuildOutputManager().findPrimaryApk(workspace,
                    request.getConfiguration().getBuildType());
            if (apk == null) {
                logManager.appendLine(
                        "Gradle succeeded but no APK was produced");
                result.setStatus(BuildStatus.FAILED);
                return result;
            }
            ApkValidator validator = new ApkValidator(appContext);
            List<String> apkProblems = validator.validate(apk,
                    request.getConfiguration());
            if (!apkProblems.isEmpty()) {
                for (String problem : apkProblems) {
                    logManager.appendLine("APK validation: " + problem);
                }
                result.setStatus(BuildStatus.FAILED);
                return result;
            }
            File dest = new BuildStorageManager(appContext).uniqueApkFile(
                    request.getProjectName() + "-"
                            + request.getConfiguration().getBuildType());
            copyFile(apk, dest);
            ApkMetadata metadata = validator.readMetadata(dest,
                    request.getConfiguration().getBuildType());
            result.setApkFile(dest);
            if (metadata != null) {
                result.setApkPackageName(metadata.packageName);
                result.setApkVersionName(metadata.versionName);
                result.setApkVersionCode(metadata.versionCode);
                result.setApkMinSdk(metadata.minSdk);
            }
            BuildDiagnosticsManager diagnostics =
                    new BuildDiagnosticsManager();
            diagnostics.analyse(logManager.getEntries());
            result.getWarnings().addAll(diagnostics.getWarnings());
            result.setStatus(BuildStatus.SUCCESS);
            return result;
        } catch (Exception e) {
            logManager.appendLine("Build failed: " + e.getMessage());
            result.setStatus(BuildStatus.FAILED);
            return result;
        } finally {
            if (workspace != null) {
                new BuildWorkspaceManager(appContext)
                        .deleteWorkspace(workspace);
            }
        }
    }

    private void copyFile(File source, File dest) throws Exception {
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
