package com.nobi.appbuilder.export;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;

import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.export.report.ExportLogManager;
import com.nobi.appbuilder.export.storage.ExportDirectoryManager;
import com.nobi.appbuilder.export.storage.ExportHistoryRepository;
import com.nobi.appbuilder.export.storage.ExportStorageManager;
import com.nobi.appbuilder.export.validation.ExportValidator;
import com.nobi.appbuilder.export.zip.ZipExporter;
import com.nobi.appbuilder.export.zip.ZipExportResult;
import com.nobi.appbuilder.export.zip.ZipValidationManager;
import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.GenerationResult;
import com.nobi.appbuilder.generator.ProjectGenerator;
import com.nobi.appbuilder.generator.templates.ProjectTemplate;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.project.ProjectSettingsManager;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Top-level export orchestrator: Phase 7 generation → validation →
 * Android Studio packaging → ZIP → verification → history. Runs off
 * the main thread; every stage reports progress. The original project
 * is never modified; the temp workspace is always cleaned up.
 */
public class ProjectPackagingManager {

    public static final String[] STAGE_LABELS = {
            "Reading project configuration",
            "Collecting generated source files",
            "Creating Android Studio structure",
            "Copying resources",
            "Validating Java files",
            "Validating XML resources",
            "Validating Gradle configuration",
            "Creating ZIP archive",
            "Verifying ZIP archive",
            "Export completed"
    };

    public interface Listener {
        void onStage(int stageIndex, String label);

        void onProgress(int percent);

        void onFinished(PackagingResult result);

        void onFailed(String message);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Context appContext;
    private final String projectId;
    private final ProjectPackagingConfig config;
    private final Listener listener;
    private final ZipExporter zipExporter = new ZipExporter();
    private volatile boolean cancelled;

    public ProjectPackagingManager(Context context, String projectId,
                                   ProjectPackagingConfig config,
                                   Listener listener) {
        this.appContext = context.getApplicationContext();
        this.projectId = projectId;
        this.config = config;
        this.listener = listener;
    }

    public void cancel() {
        cancelled = true;
        zipExporter.cancel();
    }

    public void start() {
        executor.execute(this::run);
    }

    private void run() {
        long started = System.currentTimeMillis();
        ExportLogManager logManager = new ExportLogManager(appContext);
        PackagingReport report = new PackagingReport();
        File workspace = null;
        try {
            List<String> configProblems = config.validate();
            if (!configProblems.isEmpty()) {
                fail(String.join("; ", configProblems));
                return;
            }
            stage(0);
            logManager.log("Reading project configuration");
            DatabaseHelper dbHelper = new DatabaseHelper(appContext);
            DocumentRepository repository = new DocumentRepository(dbHelper);
            IRDocument document = repository.loadDocument(projectId);
            if (document == null || document.getProject() == null) {
                dbHelper.close();
                fail("Project could not be loaded");
                return;
            }
            ProjectSettingsManager settings = new ProjectSettingsManager(
                    dbHelper, new ProjectRepository(dbHelper));
            AssetRepository assets = new AssetRepository(dbHelper);

            stage(1);
            logManager.log("Generating source with Phase 7 generator");
            GenerationContext context = new GenerationContext(document,
                    document.getProject(), config.getPackageName(),
                    config.getMinSdk(), config.getTargetSdk(),
                    config.getVersionCode(), config.getVersionName(), assets);
            GenerationResult generation =
                    new ProjectGenerator(context).generate();
            if (!generation.isSuccess()) {
                report.addAll(toPackagingErrors(generation.getReport()
                        .getErrors()));
                report.addAll(toPackagingErrors(generation.getReport()
                        .getWarnings()));
                dbHelper.close();
                finishFailed(report, started, logManager,
                        "Code generation failed — see report");
                return;
            }
            report.addAll(toPackagingErrors(
                    generation.getReport().getWarnings()));

            stage(2);
            ExportDirectoryManager directoryManager =
                    new ExportDirectoryManager(appContext);
            workspace = directoryManager.createWorkspace();
            logManager.log("Workspace: " + workspace.getAbsolutePath());

            stage(3);
            AndroidStudioProjectBuilder builder =
                    new AndroidStudioProjectBuilder(appContext);
            List<String> buildProblems = new ArrayList<>();
            AndroidStudioProjectBuilder.BuildCounts counts = builder.build(
                    workspace, config, generation, buildProblems);
            for (String problem : buildProblems) {
                report.addError(PackagingError.warning("package", null,
                        problem));
            }

            stage(4);
            stage(5);
            stage(6);
            logManager.log("Validating generated project");
            List<PackagingError> validationProblems =
                    new ExportValidator().validate(generation, config);
            report.addAll(validationProblems);
            if (report.hasErrors()) {
                dbHelper.close();
                finishFailed(report, started, logManager,
                        "Export validation failed — see report");
                return;
            }

            stage(7);
            logManager.log("Creating ZIP archive");
            ExportStorageManager storageManager =
                    new ExportStorageManager(appContext);
            if (!storageManager.hasFreeSpace(50L * 1024 * 1024)) {
                dbHelper.close();
                finishFailed(report, started, logManager,
                        "Not enough free storage for the export");
                return;
            }
            File zipFile;
            String outputUri;
            ZipExportResult zipResult;
            String treeUri = config.getOutputTreeUri();
            if (treeUri != null && !treeUri.isEmpty()) {
                zipResult = exportToSafTree(workspace, treeUri,
                        config.zipBaseName() + ".zip");
                if (zipResult == null) {
                    dbHelper.close();
                    finishFailed(report, started, logManager,
                            "Could not write to the selected output folder");
                    return;
                }
                zipFile = null;
                outputUri = lastSafDocUri == null ? null
                        : lastSafDocUri.toString();
            } else {
                zipFile = storageManager.uniqueOutputFile(
                        config.zipBaseName());
                zipResult = zipExporter.exportDirectory(workspace, zipFile,
                        (written, total) -> {
                            int pct = total > 0
                                    ? (int) (70 + 20 * written / total) : 70;
                            progress(pct);
                        });
                outputUri = Uri.fromFile(zipFile).toString();
            }
            if (cancelled) {
                dbHelper.close();
                finishFailed(report, started, logManager,
                        "Export cancelled");
                return;
            }

            stage(8);
            logManager.log("Verifying ZIP archive");
            if (zipFile != null) {
                List<String> zipProblems =
                        new ZipValidationManager().validate(zipFile);
                for (String problem : zipProblems) {
                    report.addError(PackagingError.error("zip",
                            zipFile.getName(), problem));
                }
                if (report.hasErrors()) {
                    dbHelper.close();
                    finishFailed(report, started, logManager,
                            "ZIP verification failed — see report");
                    return;
                }
                report.setZipSizeBytes(zipFile.length());
                report.setZipFileName(zipFile.getName());
            } else {
                report.setZipSizeBytes(zipResult.getBytesWritten());
                report.setZipFileName(config.zipBaseName() + ".zip");
            }

            report.setJavaFiles(counts.javaFiles);
            report.setXmlFiles(counts.xmlFiles);
            report.setResources(counts.resources);
            report.setActivities(counts.activities);
            report.setTotalFiles(counts.totalFiles);
            report.setDurationMs(System.currentTimeMillis() - started);
            stage(9);
            logManager.log("Export completed");

            PackagingResult result = new PackagingResult(config, report,
                    zipFile, outputUri);
            ExportHistoryRepository history =
                    new ExportHistoryRepository(dbHelper);
            ExportHistoryRepository.ExportRecord record =
                    new ExportHistoryRepository.ExportRecord();
            record.projectId = projectId;
            record.projectName = config.getProjectName();
            record.zipFileName = report.getZipFileName();
            record.exportDate = System.currentTimeMillis();
            record.fileSize = report.getZipSizeBytes();
            record.outputUri = outputUri;
            record.status = ExportHistoryRepository.STATUS_SUCCESS;
            record.validationSummary = report.toText();
            result.setExportHistoryId(history.insert(record));
            dbHelper.close();
            logManager.writeLogFile(config.zipBaseName());
            mainHandler.post(() -> listener.onFinished(result));
        } catch (Exception e) {
            finishFailed(report, started, logManager,
                    e.getMessage() == null ? "Export failed" : e.getMessage());
        } finally {
            if (workspace != null) {
                new ExportDirectoryManager(appContext)
                        .deleteWorkspace(workspace);
            }
        }
    }

    private Uri lastSafDocUri;

    /** Writes the ZIP into a SAF tree; returns null on failure. */
    private ZipExportResult exportToSafTree(File workspace,
                                            String treeUriString,
                                            String fileName) {
        try {
            Uri treeUri = Uri.parse(treeUriString);
            ContentResolver resolver = appContext.getContentResolver();
            Uri docUri = DocumentsContract.createDocument(resolver, treeUri,
                    "application/zip", fileName);
            if (docUri == null) {
                return null;
            }
            lastSafDocUri = docUri;
            try (OutputStream out = resolver.openOutputStream(docUri)) {
                if (out == null) {
                    return null;
                }
                return zipExporter.exportDirectoryToStream(workspace, out,
                        (written, total) -> {
                            int pct = total > 0
                                    ? (int) (70 + 20 * written / total) : 70;
                            progress(pct);
                        });
            }
        } catch (Exception e) {
            return null;
        }
    }

    private void stage(int index) {
        mainHandler.post(() -> {
            listener.onStage(index, STAGE_LABELS[index]);
            listener.onProgress(index * 10);
        });
    }

    private void progress(int percent) {
        mainHandler.post(() -> listener.onProgress(percent));
    }

    private void fail(String message) {
        mainHandler.post(() -> listener.onFailed(message));
    }

    private void finishFailed(PackagingReport report, long started,
                              ExportLogManager logManager, String message) {
        report.setDurationMs(System.currentTimeMillis() - started);
        logManager.log("FAILED: " + message);
        logManager.writeLogFile(config.zipBaseName());
        mainHandler.post(() -> listener.onFailed(
                message + "\n\n" + report.toText()));
    }

    private List<PackagingError> toPackagingErrors(
            List<com.nobi.appbuilder.generator.GenerationError> errors) {
        List<PackagingError> out = new ArrayList<>();
        for (com.nobi.appbuilder.generator.GenerationError error : errors) {
            out.add(new PackagingError(
                    error.isError() ? PackagingError.SEVERITY_ERROR
                            : PackagingError.SEVERITY_WARNING,
                    error.getStage(), error.getFile(), error.getMessage()));
        }
        return out;
    }

    public void release() {
        executor.shutdownNow();
    }
}
