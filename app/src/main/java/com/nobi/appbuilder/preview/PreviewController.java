package com.nobi.appbuilder.preview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.preview.device.DeviceProfileManager;
import com.nobi.appbuilder.preview.events.PreviewActionExecutor;
import com.nobi.appbuilder.preview.events.PreviewEventDispatcher;
import com.nobi.appbuilder.preview.factory.PreviewComponentFactory;
import com.nobi.appbuilder.preview.theme.PreviewThemeManager;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Owns a preview run: loads the document off the main thread,
 * validates and renders on the main thread, and drives screen
 * navigation. The document is loaded once and kept in memory for
 * the run; rendering never writes anything back to the database,
 * so previewing cannot change the design.
 */
public class PreviewController {

    public interface RenderListener {
        void onRendered(View rootView, List<String> errors, ScreenModel screen);

        void onLoadFailed(String message);
    }

    private final Context appContext;
    private final Context renderContext;
    private final DatabaseHelper dbHelper;
    private final PreviewSession session;
    private final PreviewThemeManager themeManager;
    private final PreviewRenderer renderer;
    private final PreviewValidator validator = new PreviewValidator();

    private final Executor backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private IRDocument document;
    private AssetRepository assetRepository;
    private PreviewContext previewContext;
    private PreviewNavigator navigator;
    private RenderListener renderListener;
    private android.app.Activity pendingActivity;

    public PreviewController(Context context, DatabaseHelper dbHelper,
                             PreviewSession session,
                             PreviewThemeManager themeManager) {
        this.appContext = context.getApplicationContext();
        // Views must be created with the activity context so Material
        // components pick up the app theme; the application context is
        // only used for database and resource lookups.
        this.renderContext = context;
        this.dbHelper = dbHelper;
        this.session = session;
        this.themeManager = themeManager;
        this.renderer = new PreviewRenderer(new PreviewComponentFactory());
    }

    public void setRenderListener(RenderListener renderListener) {
        this.renderListener = renderListener;
    }

    public PreviewSession getSession() {
        return session;
    }

    public PreviewNavigator getNavigator() {
        return navigator;
    }

    public IRDocument getDocument() {
        return document;
    }

    /** Loads the document in the background, then renders the
     * starting screen on the main thread. */
    public void loadInitial() {
        backgroundExecutor.execute(() -> {
            try {
                DocumentRepository repository = new DocumentRepository(dbHelper);
                IRDocument loaded = repository.loadDocument(session.getProjectId());
                AssetRepository assets = new AssetRepository(dbHelper);
                mainHandler.post(() -> {
                    document = loaded;
                    assetRepository = assets;
                    themeManager.setProjectTheme(
                            loaded == null ? null : loaded.getTheme());
                    previewContext = new PreviewContext(renderContext, session,
                            themeManager, assets, backgroundExecutor, mainHandler);
                    if (pendingActivity != null) {
                        previewContext.setActivity(pendingActivity);
                    }
                    PreviewEventDispatcher dispatcher =
                            new PreviewEventDispatcher(new PreviewActionExecutor());
                    previewContext.setDispatcher(dispatcher);
                    navigator = new PreviewNavigator(document, session,
                            this::renderScreen);
                    previewContext.setNavigator(navigator);
                    ScreenModel start = navigator.getStartingScreen();
                    if (start == null && document != null
                            && !document.getScreens().isEmpty()) {
                        start = document.getScreens().get(0);
                    }
                    if (start == null) {
                        if (renderListener != null) {
                            renderListener.onLoadFailed("Project has no screens");
                        }
                        return;
                    }
                    // Honour the screen the editor asked for, when it exists.
                    ScreenModel requested = document.getScreen(
                            session.getInitialScreenId());
                    if (requested != null) {
                        start = requested;
                    }
                    session.setCurrentScreenId(start.getId());
                    renderScreen(start);
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (renderListener != null) {
                        renderListener.onLoadFailed(
                                e.getMessage() == null ? "Failed to load project"
                                        : e.getMessage());
                    }
                });
            }
        });
    }

    /** Re-renders the current screen (theme change, restart, etc.). */
    public void renderCurrent() {
        ScreenModel screen = navigator == null ? null : navigator.getCurrentScreen();
        if (screen != null) {
            renderScreen(screen);
        }
    }

    /** Validates and renders one screen on the main thread. */
    private void renderScreen(ScreenModel screen) {
        if (renderListener == null || previewContext == null) {
            return;
        }
        List<String> errors = validator.validate(document, screen,
                assetRepository, session.getProjectId(), renderContext);
        View root = renderer.renderScreen(renderContext, screen, previewContext);
        renderListener.onRendered(root, errors, screen);
    }

    public void setThemeMode(int mode) {
        themeManager.setMode(mode);
        session.setThemeMode(mode);
        renderCurrent();
    }

    public void setDeviceProfile(
            com.nobi.appbuilder.preview.device.PreviewDeviceProfile profile) {
        session.setDeviceProfile(profile);
    }

    public void restart() {
        if (navigator != null) {
            navigator.restart();
        }
    }

    public void goBack() {
        if (navigator != null) {
            navigator.goBack();
        }
    }

    public void setActivity(android.app.Activity activity) {
        pendingActivity = activity;
        if (previewContext != null) {
            previewContext.setActivity(activity);
        }
    }

    /** Releases the background executor when preview closes. */
    public void release() {
        if (backgroundExecutor instanceof java.util.concurrent.ExecutorService) {
            ((java.util.concurrent.ExecutorService) backgroundExecutor).shutdownNow();
        }
    }
}
