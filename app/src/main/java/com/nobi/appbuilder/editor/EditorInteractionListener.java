package com.nobi.appbuilder.editor;

import com.nobi.appbuilder.model.ComponentModel;

/**
 * Canvas engine -&gt; EditorActivity callbacks. The engine never touches
 * the database or the JSON document directly; the activity owns saving.
 */
public interface EditorInteractionListener {

    /** Called before a mutation starts (drag, resize, add, delete). */
    void onMutationStarting();

    /** The model changed; schedule a debounced autosave / refresh info. */
    void onDocumentChanged();

    /** A gesture finished (drag/resize ended); commit + refresh hierarchy. */
    void onDocumentCommitted();

    /** Selection changed on canvas or in hierarchy. */
    void onSelectionChanged();

    /** Long-press on a component; show its context menu at screen coords. */
    void onRequestContextMenu(ComponentModel model, float screenX, float screenY);

    /** Tap on empty canvas space. */
    void onEmptyCanvasTapped();

    /** Zoom changed; update the zoom label. */
    void onZoomChanged(float zoom);

    /** Phase 5: a drag finished; build one MoveComponentCommand from it. */
    default void onDragCommitted(java.util.List<ComponentModel> models,
                                 java.util.Map<String, float[]> startPositions) {
    }

    /** Phase 5: a resize finished; build one ResizeComponentCommand. */
    default void onResizeCommitted(ComponentModel target,
                                   android.graphics.RectF startBounds) {
    }
}
