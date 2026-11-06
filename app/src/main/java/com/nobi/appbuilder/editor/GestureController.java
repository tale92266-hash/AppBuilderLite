package com.nobi.appbuilder.editor;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.utils.TouchUtils;

/**
 * The editor's touch state machine. Raw MotionEvents arrive here and are
 * routed to the active Tool (select / hand / library-drop), while pinch
 * zoom, double-tap zoom and long-press are handled by platform detectors.
 * A drag only starts after the touch slop is exceeded, so taps stay taps.
 */
public class GestureController {

    /** One interaction mode. Implemented by editor/tools/*. */
    public interface Tool {
        String getName();

        boolean onDown(GestureController controller, MotionEvent event);

        boolean onMove(GestureController controller, MotionEvent event);

        boolean onUp(GestureController controller, MotionEvent event);

        boolean onLongPress(GestureController controller, MotionEvent event);
    }

    private final Context context;
    private final DesignCanvasView view;
    private final CanvasState state;
    private final CanvasViewport viewport;
    private final SelectionController selectionController;
    private final DragController dragController;
    private final ResizeController resizeController;
    private final EditorInteractionListener listener;
    private final ScaleGestureDetector scaleDetector;
    private final GestureDetector gestureDetector;
    private final int touchSlop;

    private float downScreenX;
    private float downScreenY;
    private long downTime;
    private boolean downOnHandle;
    private boolean downOnComponent;
    private boolean pinchActive;
    private float lastPanX;
    private float lastPanY;

    public GestureController(Context context, DesignCanvasView view,
                             CanvasState state, CanvasViewport viewport,
                             SelectionController selectionController,
                             DragController dragController,
                             ResizeController resizeController,
                             EditorInteractionListener listener) {
        this.context = context;
        this.view = view;
        this.state = state;
        this.viewport = viewport;
        this.selectionController = selectionController;
        this.dragController = dragController;
        this.resizeController = resizeController;
        this.listener = listener;
        this.touchSlop = TouchUtils.getTouchSlop(context);
        this.scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        this.gestureDetector = new GestureDetector(context, new TapListener());
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

    public ResizeController getResizeController() {
        return resizeController;
    }

    public EditorInteractionListener getListener() {
        return listener;
    }

    public DesignCanvasView getView() {
        return view;
    }

    public int getTouchSlop() {
        return touchSlop;
    }

    public float designX(float screenX) {
        return viewport.getTransform().screenToDesignX(screenX);
    }

    public float designY(float screenY) {
        return viewport.getTransform().screenToDesignY(screenY);
    }

    public void invalidate() {
        view.invalidate();
    }

    public void setTool(Tool tool) {
        state.setCurrentTool(tool);
    }

    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        gestureDetector.onTouchEvent(event);
        Tool tool = state.getCurrentTool();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                pinchActive = false;
                downScreenX = event.getX();
                downScreenY = event.getY();
                downTime = event.getEventTime();
                lastPanX = event.getX();
                lastPanY = event.getY();
                downOnHandle = selectionController.getHandleAt(event.getX(), event.getY())
                        != SelectionController.NO_HANDLE;
                downOnComponent = hitComponentAt(event.getX(), event.getY()) != null;
                if (tool != null) {
                    return tool.onDown(this, event);
                }
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() == 2) {
                    pinchActive = true;
                    cancelActiveGesture();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (pinchActive) {
                    return true;
                }
                if (tool != null) {
                    return tool.onMove(this, event);
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pinchActive = false;
                boolean handled = tool != null && tool.onUp(this, event);
                finishGesture(event);
                return handled;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() <= 2) {
                    pinchActive = false;
                }
                return true;
            default:
                return true;
        }
    }

    private void finishGesture(MotionEvent event) {
        if (dragController.isDragging()) {
            boolean moved = dragController.endDrag();
            if (moved) {
                listener.onDocumentCommitted();
            }
            invalidate();
        }
        if (resizeController.isActive()) {
            resizeController.endResize();
            listener.onDocumentCommitted();
            invalidate();
        }
    }

    private void cancelActiveGesture() {
        if (dragController.isDragging()) {
            dragController.endDrag();
        }
        if (resizeController.isActive()) {
            resizeController.endResize();
        }
    }

    /** Component under a screen point, or null. */
    public ComponentModel hitComponentAt(float screenX, float screenY) {
        ComponentModel root = state.getRoot();
        if (root == null) {
            return null;
        }
        android.graphics.RectF screenBounds = new android.graphics.RectF(0, 0,
                state.getDesignWidthDp(), state.getDesignHeightDp());
        return ComponentHitTester.hitTest(root, designX(screenX), designY(screenY),
                screenBounds);
    }

    public boolean isTap(MotionEvent event) {
        return TouchUtils.isTap(downScreenX, downScreenY, event.getX(), event.getY(),
                touchSlop, downTime, event.getEventTime());
    }

    public boolean movedBeyondSlop(MotionEvent event) {
        return TouchUtils.distance(downScreenX, downScreenY, event.getX(), event.getY())
                > touchSlop;
    }

    public float getDownScreenX() {
        return downScreenX;
    }

    public float getDownScreenY() {
        return downScreenY;
    }

    public boolean isDownOnHandle() {
        return downOnHandle;
    }

    public boolean isDownOnComponent() {
        return downOnComponent;
    }

    public float getLastPanX() {
        return lastPanX;
    }

    public float getLastPanY() {
        return lastPanY;
    }

    public void setLastPan(float x, float y) {
        lastPanX = x;
        lastPanY = y;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            viewport.pinchZoom(detector.getFocusX(), detector.getFocusY(),
                    detector.getScaleFactor());
            listener.onZoomChanged(viewport.getZoom());
            invalidate();
            return true;
        }
    }

    private class TapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent event) {
            viewport.doubleTapZoom(event.getX(), event.getY());
            listener.onZoomChanged(viewport.getZoom());
            invalidate();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            ComponentModel hit = hitComponentAt(event.getX(), event.getY());
            if (hit != null && hit != state.getRoot()) {
                if (!state.isSelected(hit.getId())) {
                    state.select(hit.getId());
                    listener.onSelectionChanged();
                    invalidate();
                }
                listener.onRequestContextMenu(hit, event.getX(), event.getY());
            }
            Tool tool = state.getCurrentTool();
            if (tool != null) {
                tool.onLongPress(GestureController.this, event);
            }
        }
    }
}
