package com.nobi.appbuilder.ui.code;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.GenerationResult;
import com.nobi.appbuilder.generator.ProjectGenerator;
import com.nobi.appbuilder.generator.templates.ProjectTemplate;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.project.ProjectSettingsManager;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runs ProjectGenerator off the main thread and delivers the result
 * back on it. Settings come from ProjectSettingsManager with the
 * ProjectTemplate defaults; the package name always comes from the
 * project itself.
 */
public class CodeGenerationController {

    public interface Listener {
        void onGenerated(GenerationResult result);

        void onFailed(String message);
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Context appContext;
    private final String projectId;
    private final Listener listener;

    private GenerationResult lastResult;

    public CodeGenerationController(Context context, String projectId,
                                    Listener listener) {
        this.appContext = context.getApplicationContext();
        this.projectId = projectId;
        this.listener = listener;
    }

    public GenerationResult getLastResult() {
        return lastResult;
    }

    public void generate() {
        executor.execute(() -> {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper(appContext);
                DocumentRepository repository = new DocumentRepository(dbHelper);
                IRDocument document = repository.loadDocument(projectId);
                if (document == null || document.getProject() == null) {
                    postFailed("Project could not be loaded");
                    dbHelper.close();
                    return;
                }
                ProjectSettingsManager settings =
                        new ProjectSettingsManager(dbHelper,
                                new com.nobi.appbuilder.database
                                        .ProjectRepository(dbHelper));
                String versionName = settings.getSetting(projectId,
                        ProjectSettingsManager.KEY_VERSION_NAME, "1.0");
                int versionCode = parseInt(settings.getSetting(projectId,
                        ProjectSettingsManager.KEY_VERSION_CODE, "1"), 1);
                int targetSdk = parseInt(settings.getSetting(projectId,
                        ProjectSettingsManager.KEY_TARGET_SDK,
                        String.valueOf(ProjectTemplate.DEFAULT_TARGET_SDK)),
                        ProjectTemplate.DEFAULT_TARGET_SDK);
                AssetRepository assets = new AssetRepository(dbHelper);
                GenerationContext context = new GenerationContext(
                        document, document.getProject(),
                        document.getProject().getPackageName(),
                        ProjectTemplate.DEFAULT_MIN_SDK, targetSdk,
                        versionCode, versionName, assets);
                GenerationResult result =
                        new ProjectGenerator(context).generate();
                dbHelper.close();
                lastResult = result;
                mainHandler.post(() -> listener.onGenerated(result));
            } catch (Exception e) {
                postFailed(e.getMessage() == null ? "Generation failed"
                        : e.getMessage());
            }
        });
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private void postFailed(String message) {
        mainHandler.post(() -> listener.onFailed(message));
    }

    public void release() {
        executor.shutdownNow();
    }
}
