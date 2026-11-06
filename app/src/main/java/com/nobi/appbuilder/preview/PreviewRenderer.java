package com.nobi.appbuilder.preview;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.preview.factory.PreviewComponentFactory;

/**
 * Turns a screen's ComponentModel tree into a real Android View
 * hierarchy. Before creating anything it pre-assigns a stable
 * Android view id to every component, so ConstraintLayout targets
 * can resolve siblings no matter what order they are built in.
 * The root view fills its container (the device frame); children
 * keep the dimensions from the JSON.
 */
public class PreviewRenderer {

    private final PreviewComponentFactory factory;

    public PreviewRenderer(PreviewComponentFactory factory) {
        this.factory = factory;
    }

    /**
     * Spec-shaped entry point: renders one component tree with a
     * default context (no session, no assets, project theme only).
     * The controller uses {@link #renderScreen} instead.
     */
    public View render(Context context, ComponentModel component) {
        PreviewSession session = new PreviewSession(null, null,
                new com.nobi.appbuilder.preview.device.DeviceProfileManager()
                        .getDefault());
        PreviewContext previewContext = new PreviewContext(context, session,
                new com.nobi.appbuilder.preview.theme.PreviewThemeManager(null),
                null, Runnable::run, new android.os.Handler(
                        android.os.Looper.getMainLooper()));
        previewContext.setDispatcher(
                new com.nobi.appbuilder.preview.events.PreviewEventDispatcher(
                        new com.nobi.appbuilder.preview.events
                                .PreviewActionExecutor()));
        return render(context, component, previewContext);
    }

    public View render(Context context, ComponentModel component,
                       PreviewContext previewContext) {
        previewContext.clearViewRegistries();
        assignViewIds(component, previewContext);
        View root = factory.create(context, component, previewContext);
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        previewContext.getThemeManager().applyRootBackground(root);
        return root;
    }

    /** Renders a whole screen (its root component). */
    public View renderScreen(Context context, ScreenModel screen,
                             PreviewContext previewContext) {
        if (screen == null || screen.getRootComponent() == null) {
            return PreviewComponentFactory.errorView(context,
                    screen == null ? "?" : screen.getId(),
                    "screen has no root component");
        }
        return render(context, screen.getRootComponent(), previewContext);
    }

    private void assignViewIds(ComponentModel model, PreviewContext context) {
        if (model == null || model.getId() == null) {
            return;
        }
        context.assignViewId(model.getId());
        for (ComponentModel child : model.getChildren()) {
            assignViewIds(child, context);
        }
    }
}
