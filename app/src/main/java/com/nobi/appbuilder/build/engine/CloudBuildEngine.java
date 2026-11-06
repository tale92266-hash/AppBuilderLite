package com.nobi.appbuilder.build.engine;

import android.content.Context;

import com.nobi.appbuilder.build.BuildCancellationManager;
import com.nobi.appbuilder.build.BuildConfiguration;
import com.nobi.appbuilder.build.BuildRequest;
import com.nobi.appbuilder.build.BuildResult;
import com.nobi.appbuilder.build.BuildStatus;
import com.nobi.appbuilder.build.BuildTask;
import com.nobi.appbuilder.build.cloud.CloudBuildSettings;
import com.nobi.appbuilder.build.cloud.CloudBuildWorkflow;
import com.nobi.appbuilder.build.cloud.GitHubClient;
import com.nobi.appbuilder.build.logs.BuildLogManager;
import com.nobi.appbuilder.build.storage.BuildStorageManager;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.export.PackagingResult;
import com.nobi.appbuilder.export.ProjectPackagingConfig;
import com.nobi.appbuilder.export.ProjectPackagingManager;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.project.ProjectSettingsManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Phase 15 — cloud build. Exports the project ZIP exactly like the
 * external engine, uploads it to the user's private GitHub build
 * repo, triggers the Actions workflow, polls for the run, downloads
 * the APK artifact and verifies it. The result is SUCCESS only
 * when a real, verified APK file is on disk — anything else is
 * FAILED, never a half-success. Needs internet and a connected
 * GitHub account (the Build screen asks for it before starting).
 */
public class CloudBuildEngine implements BuildEngine {

    /** Contents API refuses files over 100 MB; stay well under it. */
    public static final long MAX_ZIP_BYTES = 50L * 1024 * 1024;
    private static final long RUN_APPEAR_TIMEOUT_MS = 120000;
    private static final long BUILD_TIMEOUT_MS = 20L * 60 * 1000;
    private static final long POLL_MS = 15000;

    private final Context appContext;

    public CloudBuildEngine(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @Override
    public boolean supports(
            com.nobi.appbuilder.build.BuildEnvironmentInfo environment) {
        return new CloudBuildSettings(appContext).isConnected();
    }

    @Override
    public BuildResult execute(BuildRequest request,
                               BuildLogManager logManager,
                               BuildCancellationManager cancellation,
                               EngineListener listener) {
        BuildResult result = new BuildResult(request.getBuildId(),
                request.getProjectId(), request.getConfiguration());
        result.setStatus(BuildStatus.RUNNING);
        CloudBuildSettings settings = new CloudBuildSettings(appContext);
        if (!settings.isConnected()) {
            logManager.appendLine("GitHub is not connected — "
                    + "connect it on the Build screen first.");
            result.setStatus(BuildStatus.FAILED);
            return result;
        }
        BuildConfiguration config = request.getConfiguration();
        listener.onTask(BuildTask.PREPARE_WORKSPACE);

        File zipFile = exportZip(request, logManager, listener);
        if (zipFile == null) {
            logManager.appendLine("Export failed — nothing was uploaded.");
            result.setStatus(BuildStatus.FAILED);
            return result;
        }
        result.setExternalZipPath(zipFile.getAbsolutePath());
        if (zipFile.length() > MAX_ZIP_BYTES) {
            logManager.appendLine("Project ZIP is over 50 MB — "
                    + "too large for cloud build.");
            result.setStatus(BuildStatus.FAILED);
            return result;
        }

        try {
            GitHubClient client = new GitHubClient(settings.getToken());
            String owner = settings.getOwner();
            String repo = settings.getRepo();
            ensureRepoReady(client, owner, repo, logManager);
            String branch = client.defaultBranch(owner, repo);

            listener.onTask(BuildTask.UPLOAD_PROJECT);
            listener.onProgress(35);
            logManager.appendLine("Uploading project (" + zipFile.length()
                    + " bytes) to " + owner + "/" + repo + "…");
            byte[] zipBytes = Files.readAllBytes(zipFile.toPath());
            String headSha = client.putFile(owner, repo,
                    CloudBuildWorkflow.PROJECT_ZIP_PATH, zipBytes,
                    "Cloud build: " + request.getProjectName());
            logManager.appendLine("Project uploaded.");

            listener.onTask(BuildTask.CLOUD_BUILD);
            listener.onProgress(45);
            client.dispatchWorkflow(owner, repo,
                    CloudBuildWorkflow.WORKFLOW_FILE, branch);
            logManager.appendLine("Cloud build started.");

            JSONObject run = waitForRun(client, owner, repo, headSha,
                    cancellation, request.getBuildId(), logManager);
            if (run == null) {
                if (cancellation.isCancelled(request.getBuildId())) {
                    result.setStatus(BuildStatus.CANCELLED);
                    return result;
                }
                logManager.appendLine("Cloud build did not start in time.");
                result.setStatus(BuildStatus.FAILED);
                return result;
            }
            long runId = run.getLong("id");
            logManager.appendLine("Build running (run " + runId + ")…");
            String conclusion = waitForCompletion(client, owner, repo, runId,
                    cancellation, request.getBuildId(), logManager, listener);
            if (conclusion == null) {
                logManager.appendLine("Cloud build was cancelled.");
                result.setStatus(BuildStatus.CANCELLED);
                return result;
            }
            if (!"success".equals(conclusion)) {
                logManager.appendLine("Cloud build failed on the server "
                        + "(" + conclusion + ") — see the run log on GitHub.");
                result.setStatus(BuildStatus.FAILED);
                return result;
            }

            listener.onTask(BuildTask.DOWNLOAD_APK);
            listener.onProgress(90);
            File apkFile = downloadApk(client, owner, repo, runId,
                    request, logManager);
            if (apkFile == null) {
                logManager.appendLine("Cloud build finished without an APK.");
                result.setStatus(BuildStatus.FAILED);
                return result;
            }
            listener.onTask(BuildTask.VERIFY_APK);
            listener.onProgress(95);
            if (!verifyApk(apkFile)) {
                logManager.appendLine("Downloaded APK failed verification.");
                result.setStatus(BuildStatus.FAILED);
                return result;
            }
            result.setApkFile(apkFile);
            result.setApkPackageName(config.getApplicationId());
            result.setApkVersionName(config.getVersionName());
            result.setApkVersionCode(config.getVersionCode());
            result.setApkMinSdk(config.getMinSdk());
            logManager.appendLine("APK ready: " + apkFile.getName()
                    + " (" + apkFile.length() + " bytes)");
            listener.onProgress(100);
            result.setStatus(BuildStatus.SUCCESS);
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            result.setStatus(BuildStatus.CANCELLED);
            return result;
        } catch (Exception e) {
            logManager.appendLine("Cloud build error: " + e.getMessage());
            result.setStatus(BuildStatus.FAILED);
            return result;
        }
    }

    /** Creates the repo + workflow + debug keystore on first use. */
    private void ensureRepoReady(GitHubClient client, String owner,
                                 String repo, BuildLogManager logManager)
            throws IOException {
        if (client.getRepo(owner, repo) == null) {
            logManager.appendLine("Creating private build repo " + owner
                    + "/" + repo + "…");
            client.createRepo(repo);
        }
        if (client.getFileSha(owner, repo,
                CloudBuildWorkflow.WORKFLOW_PATH) == null) {
            logManager.appendLine("Installing cloud build workflow…");
            client.putFile(owner, repo, CloudBuildWorkflow.WORKFLOW_PATH,
                    CloudBuildWorkflow.yaml().getBytes(
                            StandardCharsets.UTF_8),
                    "Add AppBuilder cloud build workflow");
        }
        if (client.getFileSha(owner, repo,
                CloudBuildWorkflow.KEYSTORE_PATH) == null) {
            logManager.appendLine("Installing fixed debug keystore…");
            byte[] keystore = readAsset(CloudBuildWorkflow.KEYSTORE_ASSET);
            client.putFile(owner, repo, CloudBuildWorkflow.KEYSTORE_PATH,
                    keystore, "Add fixed debug keystore for cloud builds");
        }
    }

    private byte[] readAsset(String assetPath) throws IOException {
        try (InputStream in = appContext.getAssets().open(assetPath);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        }
    }

    /** Waits until the dispatched run appears (matched by head SHA). */
    private JSONObject waitForRun(GitHubClient client, String owner,
                                  String repo, String headSha,
                                  BuildCancellationManager cancellation,
                                  String buildId, BuildLogManager logManager)
            throws IOException, InterruptedException {
        long deadline = System.currentTimeMillis() + RUN_APPEAR_TIMEOUT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (cancellation.isCancelled(buildId)) {
                return null;
            }
            JSONObject run = client.findRunByHeadSha(owner, repo,
                    CloudBuildWorkflow.WORKFLOW_FILE, headSha);
            if (run != null) {
                return run;
            }
            sleepChunk(5000);
        }
        return null;
    }

    /** Polls a run until it completes; returns its conclusion. */
    private String waitForCompletion(GitHubClient client, String owner,
                                     String repo, long runId,
                                     BuildCancellationManager cancellation,
                                     String buildId,
                                     BuildLogManager logManager,
                                     EngineListener listener)
            throws IOException, InterruptedException {
        long deadline = System.currentTimeMillis() + BUILD_TIMEOUT_MS;
        long started = System.currentTimeMillis();
        while (System.currentTimeMillis() < deadline) {
            if (cancellation.isCancelled(buildId)) {
                return null;
            }
            JSONObject run = client.getRun(owner, repo, runId);
            String status = run.optString("status", "");
            if ("completed".equals(status)) {
                return run.optString("conclusion", "failure");
            }
            long elapsed = System.currentTimeMillis() - started;
            int progress = 50 + (int) Math.min(35,
                    elapsed / 30000);
            listener.onProgress(progress);
            sleepChunk(POLL_MS);
        }
        logManager.appendLine("Cloud build timed out after 20 minutes.");
        return "timed_out";
    }

    /** Downloads the cloud-apk artifact and extracts the APK. */
    private File downloadApk(GitHubClient client, String owner, String repo,
                             long runId, BuildRequest request,
                             BuildLogManager logManager) throws IOException {
        JSONArray artifacts = client.listArtifacts(owner, repo, runId);
        String downloadUrl = null;
        if (artifacts != null) {
            for (int i = 0; i < artifacts.length(); i++) {
                JSONObject artifact = artifacts.getJSONObject(i);
                if (CloudBuildWorkflow.ARTIFACT_NAME.equals(
                        artifact.optString("name"))
                        && !artifact.optBoolean("expired", false)) {
                    downloadUrl = artifact.optString("archive_download_url");
                    break;
                }
            }
        }
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            return null;
        }
        File cacheDir = appContext.getCacheDir();
        File artifactZip = new File(cacheDir,
                "cloud-apk-" + request.getBuildId() + ".zip");
        client.downloadTo(downloadUrl, artifactZip);
        String baseName = sanitize(request.getProjectName()) + "-cloud";
        File dest = new BuildStorageManager(appContext)
                .uniqueApkFile(baseName);
        if (!extractApk(artifactZip, dest)) {
            return null;
        }
        artifactZip.delete();
        return dest;
    }

    /** First .apk entry of the artifact ZIP, streamed to dest. */
    private boolean extractApk(File artifactZip, File dest) {
        try (ZipInputStream in = new ZipInputStream(
                new FileInputStream(artifactZip));
             FileOutputStream out = new FileOutputStream(dest)) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (!entry.isDirectory()
                        && entry.getName().endsWith(".apk")) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    return dest.length() > 0;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** A real APK: a ZIP that contains AndroidManifest.xml. */
    private boolean verifyApk(File apkFile) {
        if (apkFile == null || !apkFile.exists() || apkFile.length() == 0) {
            return false;
        }
        try (ZipFile zip = new ZipFile(apkFile)) {
            return zip.getEntry("AndroidManifest.xml") != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String sanitize(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "app";
        }
        String clean = name.trim().replaceAll("[^A-Za-z0-9._-]+", "-");
        return clean.isEmpty() ? "app" : clean;
    }

    private void sleepChunk(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }

    /** Same export step as the external engine: ZIP or null. */
    private File exportZip(BuildRequest request, BuildLogManager logManager,
                           EngineListener listener) {
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
        logManager.appendLine("Exporting the project for the cloud build…");
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<PackagingResult> success = new AtomicReference<>();
        AtomicReference<String> failure = new AtomicReference<>();
        ProjectPackagingManager manager = new ProjectPackagingManager(
                appContext, request.getProjectId(), config,
                new ProjectPackagingManager.Listener() {
                    @Override
                    public void onStage(int index, String label) {
                        logManager.appendLine(label);
                        listener.onProgress(index);
                    }

                    @Override
                    public void onProgress(int percent) {
                        listener.onProgress(percent / 3);
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
            return null;
        }
        manager.release();
        if (success.get() != null && success.get().getZipFile() != null) {
            return success.get().getZipFile();
        }
        logManager.appendLine("Export failed: "
                + (failure.get() == null ? "unknown error" : failure.get()));
        return null;
    }
}
