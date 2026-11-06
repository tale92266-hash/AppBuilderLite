package com.nobi.appbuilder.preview;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;

import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.preview.events.PreviewEventDispatcher;
import com.nobi.appbuilder.preview.theme.PreviewThemeManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Everything a preview render needs, carried in one object so the
 * factories and component previews never reach for globals. Holds the
 * session (project/screen/theme/device), the theme manager, the
 * navigator and dispatcher (set once the controller exists), the
 * component-id -&gt; View registry used by event actions
 * (changeText / validateInput / changeVisibility) and by
 * ConstraintLayout target resolution, plus the background executor
 * and main-thread handler used for async image loading.
 */
public class PreviewContext {

    private final Context androidContext;
    private final PreviewSession session;
    private final PreviewThemeManager themeManager;
    private final AssetRepository assetRepository;
    private final Executor backgroundExecutor;
    private final Handler mainHandler;

    private PreviewNavigator navigator;
    private PreviewEventDispatcher dispatcher;
    private Activity activity;

    private final Map<String, View> viewsByComponentId = new HashMap<>();
    private final Map<String, Integer> viewIdsByComponentId = new HashMap<>();

    public PreviewContext(Context androidContext, PreviewSession session,
                          PreviewThemeManager themeManager,
                          AssetRepository assetRepository,
                          Executor backgroundExecutor, Handler mainHandler) {
        this.androidContext = androidContext;
        this.session = session;
        this.themeManager = themeManager;
        this.assetRepository = assetRepository;
        this.backgroundExecutor = backgroundExecutor;
        this.mainHandler = mainHandler;
    }

    public Context getAndroidContext() {
        return androidContext;
    }

    public PreviewSession getSession() {
        return session;
    }

    public PreviewThemeManager getThemeManager() {
        return themeManager;
    }

    public AssetRepository getAssetRepository() {
        return assetRepository;
    }

    public Executor getBackgroundExecutor() {
        return backgroundExecutor;
    }

    public Handler getMainHandler() {
        return mainHandler;
    }

    public PreviewNavigator getNavigator() {
        return navigator;
    }

    public void setNavigator(PreviewNavigator navigator) {
        this.navigator = navigator;
    }

    public PreviewEventDispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(PreviewEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    /** Stable Android view id for a component id (pre-assigned so
     * ConstraintLayout targets can resolve siblings in any order). */
    public int assignViewId(String componentId) {
        Integer existing = viewIdsByComponentId.get(componentId);
        if (existing != null) {
            return existing;
        }
        int id = View.generateViewId();
        viewIdsByComponentId.put(componentId, id);
        return id;
    }

    public int getViewId(String componentId) {
        Integer id = viewIdsByComponentId.get(componentId);
        return id == null ? View.NO_ID : id;
    }

    public void registerView(String componentId, View view) {
        if (componentId != null && view != null) {
            viewsByComponentId.put(componentId, view);
        }
    }

    public View findView(String componentId) {
        return viewsByComponentId.get(componentId);
    }

    /** Clears the per-render registries before a fresh render. */
    public void clearViewRegistries() {
        viewsByComponentId.clear();
        viewIdsByComponentId.clear();
    }
}
