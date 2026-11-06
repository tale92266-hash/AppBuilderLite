package com.nobi.appbuilder.editor;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.editor.tools.DragTool;
import com.nobi.appbuilder.editor.tools.HandTool;
import com.nobi.appbuilder.editor.tools.SelectionTool;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.ArrayList;
import java.util.List;

/**
 * The design canvas: a custom View whose engine (renderer, viewport,
 * gesture controllers) lives in the editor package. Positions are
 * stored in design dp; this View only maps them to the screen.
 */
public class DesignCanvasView extends View {

    private CanvasRenderer renderer;
    private CanvasViewport viewport;
    private GestureController gestureController;
    private SelectionController selectionController;
    private DragController dragController;
    private ResizeController resizeController;
    private CanvasState state;
    private EditorInteractionListener listener;

    public DesignCanvasView(Context context) {
        super(context);
        initialize();
    }

    public DesignCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    private void initialize() {
        float density = getResources().getDisplayMetrics().density;
        state = new CanvasState();
        viewport = new CanvasViewport(density, state.getDesignWidthDp(),
                state.getDesignHeightDp());
        renderer = new CanvasRenderer();
        selectionController = new SelectionController(state, viewport);
        dragController = new DragController(state, viewport);
        resizeController = new ResizeController(state);
        gestureController = new GestureController(getContext(), this, state, viewport,
                selectionController, dragController, resizeController,
                new EditorInteractionListener() {
                    @Override
                    public void onMutationStarting() {
                        if (listener != null) {
                            listener.onMutationStarting();
                        }
                    }

                    @Override
                    public void onDocumentChanged() {
                        if (listener != null) {
                            listener.onDocumentChanged();
                        }
                    }

                    @Override
                    public void onDocumentCommitted() {
                        if (listener != null) {
                            listener.onDocumentCommitted();
                        }
                    }

                    @Override
                    public void onSelectionChanged() {
                        if (listener != null) {
                            listener.onSelectionChanged();
                        }
                    }

                    @Override
                    public void onRequestContextMenu(ComponentModel model,
                                                     float screenX, float screenY) {
                        if (listener != null) {
                            listener.onRequestContextMenu(model, screenX, screenY);
                        }
                    }

                    @Override
                    public void onEmptyCanvasTapped() {
                        if (listener != null) {
                            listener.onEmptyCanvasTapped();
                        }
                    }

                    @Override
                    public void onZoomChanged(float zoom) {
                        if (listener != null) {
                            listener.onZoomChanged(zoom);
                        }
                    }
                });
        state.setCurrentTool(new SelectionTool());
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setListener(EditorInteractionListener listener) {
        this.listener = listener;
    }

    public CanvasState getState() {
        return state;
    }

    public CanvasViewport getViewport() {
        return viewport;
    }

    public SelectionController getSelectionController() {
        return selectionController;
    }

    public DragController getDragController() {
        return dragController;
    }

    /** Opens a screen in the editor and fits it on screen. */
    public void setScreen(ScreenModel screen, ComponentModel root) {
        state.setScreen(screen);
        state.setRoot(root);
        viewport.fitToScreen();
        invalidate();
    }

    public void useSelectionTool() {
        state.setCurrentTool(new SelectionTool());
        invalidate();
    }

    public void useHandTool() {
        state.setCurrentTool(new HandTool());
        invalidate();
    }

    /** Arms tap-to-add for one library component type. */
    public void armLibraryDrop(String type) {
        state.setCurrentTool(new DragTool(type));
        invalidate();
    }

    public void zoomIn() {
        viewport.zoomIn();
        if (listener != null) {
            listener.onZoomChanged(viewport.getZoom());
        }
        invalidate();
    }

    public void zoomOut() {
        viewport.zoomOut();
        if (listener != null) {
            listener.onZoomChanged(viewport.getZoom());
        }
        invalidate();
    }

    public void resetZoom() {
        viewport.resetZoom();
        if (listener != null) {
            listener.onZoomChanged(viewport.getZoom());
        }
        invalidate();
    }

    public void fitToScreen() {
        viewport.fitToScreen();
        if (listener != null) {
            listener.onZoomChanged(viewport.getZoom());
        }
        invalidate();
    }

    public void setGridVisible(boolean visible) {
        state.setGridVisible(visible);
        viewport.setGridVisible(visible);
        invalidate();
    }

    public void setSnapEnabled(boolean enabled) {
        state.setSnapEnabled(enabled);
        viewport.setSnapEnabled(enabled);
        invalidate();
    }

    public List<ComponentModel> getSelectedModels() {
        return state.getSelectedModels();
    }

    /** Deletes the selection (root can never be deleted, locked models
     * are skipped). */
    public boolean deleteSelected() {
        List<ComponentModel> selected = state.getSelectedModels();
        if (selected.isEmpty()) {
            return false;
        }
        List<ComponentModel> deletable = new ArrayList<>();
        for (ComponentModel model : selected) {
            if (!model.isLocked()) {
                deletable.add(model);
            }
        }
        if (deletable.isEmpty()) {
            return false;
        }
        if (listener != null) {
            listener.onMutationStarting();
        }
        for (ComponentModel model : deletable) {
            ComponentModel parent = ComponentHitTester.findParent(state.getRoot(),
                    model.getId());
            if (parent != null) {
                parent.getChildren().remove(model);
            }
        }
        state.clearSelection();
        if (listener != null) {
            listener.onSelectionChanged();
            listener.onDocumentCommitted();
        }
        invalidate();
        return true;
    }

    /** Duplicates each selected model (fresh ids) next to the original. */
    public boolean duplicateSelected() {
        List<ComponentModel> selected = state.getSelectedModels();
        if (selected.isEmpty()) {
            return false;
        }
        if (listener != null) {
            listener.onMutationStarting();
        }
        ComponentModel lastCopy = null;
        for (ComponentModel model : selected) {
            ComponentModel copy = ComponentFactory.cloneTree(model);
            copy.setX(model.getX() + 16);
            copy.setY(model.getY() + 16);
            ComponentModel parent = ComponentHitTester.findParent(state.getRoot(),
                    model.getId());
            if (parent == null) {
                parent = state.getRoot();
            }
            parent.addChild(copy);
            lastCopy = copy;
        }
        if (lastCopy != null) {
            state.select(lastCopy.getId());
        }
        if (listener != null) {
            listener.onSelectionChanged();
            listener.onDocumentCommitted();
        }
        invalidate();
        return true;
    }

    public boolean copySelected() {
        ComponentModel single = state.getSingleSelection();
        if (single == null) {
            return false;
        }
        state.setClipboard(ComponentFactory.cloneTree(single));
        return true;
    }

    /** Pastes the clipboard copy at an offset; returns the pasted model. */
    public ComponentModel pasteClipboard() {
        ComponentModel clipboard = state.getClipboard();
        if (clipboard == null || state.getRoot() == null) {
            return null;
        }
        if (listener != null) {
            listener.onMutationStarting();
        }
        ComponentModel copy = ComponentFactory.cloneTree(clipboard);
        copy.setX(copy.getX() + 16);
        copy.setY(copy.getY() + 16);
        ComponentModel parent = ComponentHitTester.findParent(state.getRoot(),
                clipboard.getId());
        if (parent == null) {
            parent = state.getRoot();
        }
        parent.addChild(copy);
        state.select(copy.getId());
        if (listener != null) {
            listener.onSelectionChanged();
            listener.onDocumentCommitted();
        }
        invalidate();
        return copy;
    }

    /** Moves a model one step later (forward) or earlier (backward) in its parent. */
    public boolean reorder(ComponentModel model, boolean forward) {
        if (model.isLocked()) {
            return false;
        }
        ComponentModel parent = ComponentHitTester.findParent(state.getRoot(),
                model.getId());
        if (parent == null) {
            return false;
        }
        List<ComponentModel> children = parent.getChildren();
        int index = children.indexOf(model);
        int swap = forward ? index + 1 : index - 1;
        if (index < 0 || swap < 0 || swap >= children.size()) {
            return false;
        }
        if (listener != null) {
            listener.onMutationStarting();
        }
        children.set(index, children.get(swap));
        children.set(swap, model);
        if (listener != null) {
            listener.onDocumentCommitted();
        }
        invalidate();
        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewport.onSizeChanged(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        renderer.draw(canvas, state, viewport, dragController.getCurrentGuides());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureController.onTouchEvent(event);
    }

    @Override
    public boolean onDragEvent(DragEvent event) {
        if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
            return event.getClipDescription() != null
                    && event.getClipDescription().hasMimeType("text/plain");
        }
        if (event.getAction() == DragEvent.ACTION_DROP) {
            ClipData clipData = event.getClipData();
            if (clipData == null || clipData.getItemCount() == 0) {
                return false;
            }
            CharSequence type = clipData.getItemAt(0).getText();
            if (type == null) {
                return false;
            }
            if (listener != null) {
                listener.onMutationStarting();
            }
            float designX = viewport.getTransform().screenToDesignX(event.getX());
            float designY = viewport.getTransform().screenToDesignY(event.getY());
            ComponentModel created = dragController.dropNewComponent(
                    type.toString(), designX, designY);
            if (created != null) {
                state.select(created.getId());
                if (listener != null) {
                    listener.onSelectionChanged();
                    listener.onDocumentCommitted();
                }
            }
            invalidate();
            return true;
        }
        return true;
    }
}
