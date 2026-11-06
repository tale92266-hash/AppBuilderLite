package com.nobi.appbuilder.editor.tools;

import android.graphics.RectF;
import android.view.MotionEvent;

import com.nobi.appbuilder.editor.GestureController;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.selection.MultiSelectionManager;

/**
 * Default tool: tap to select, drag to move, handles to resize.
 * A drag only starts after the touch slop is exceeded, so a tap never
 * accidentally moves a component. Dragging on empty canvas draws a
 * marquee rectangle and selects every component it touches (Phase 5).
 */
public class SelectionTool implements GestureController.Tool {

    private boolean marqueeActive;
    private float marqueeStartX;
    private float marqueeStartY;

    @Override
    public String getName() {
        return "select";
    }

    @Override
    public boolean onDown(GestureController controller, MotionEvent event) {
        marqueeActive = false;
        if (controller.isDownOnHandle()) {
            ComponentModel selected = controller.getState().getSingleSelection();
            int handle = controller.getSelectionController()
                    .getHandleAt(event.getX(), event.getY());
            if (selected != null && handle != -1) {
                controller.getListener().onMutationStarting();
                controller.getResizeController().beginResize(selected, handle,
                        controller.designX(event.getX()),
                        controller.designY(event.getY()));
            }
            return true;
        }
        if (controller.isDownOnComponent()) {
            ComponentModel hit = controller.hitComponentAt(event.getX(), event.getY());
            if (hit == null || hit == controller.getState().getRoot()) {
                controller.getState().clearSelection();
                controller.getListener().onSelectionChanged();
            } else if (!controller.getState().isSelected(hit.getId())) {
                controller.getState().select(hit.getId());
                controller.getListener().onSelectionChanged();
            }
            controller.invalidate();
            return true;
        }
        marqueeActive = true;
        marqueeStartX = controller.designX(event.getX());
        marqueeStartY = controller.designY(event.getY());
        controller.getState().setMarqueeRect(
                new RectF(marqueeStartX, marqueeStartY, marqueeStartX, marqueeStartY));
        controller.invalidate();
        return true;
    }

    @Override
    public boolean onMove(GestureController controller, MotionEvent event) {
        if (controller.getResizeController().isActive()) {
            controller.getResizeController().updateResize(
                    controller.designX(event.getX()),
                    controller.designY(event.getY()));
            controller.getListener().onDocumentChanged();
            controller.invalidate();
            return true;
        }
        if (controller.getDragController().isDragging()) {
            controller.getDragController().updateDrag(
                    controller.designX(event.getX()),
                    controller.designY(event.getY()));
            controller.getListener().onDocumentChanged();
            controller.invalidate();
            return true;
        }
        if (marqueeActive) {
            float x = controller.designX(event.getX());
            float y = controller.designY(event.getY());
            controller.getState().setMarqueeRect(new RectF(
                    Math.min(marqueeStartX, x), Math.min(marqueeStartY, y),
                    Math.max(marqueeStartX, x), Math.max(marqueeStartY, y)));
            controller.invalidate();
            return true;
        }
        if (controller.isDownOnComponent() && controller.movedBeyondSlop(event)
                && controller.getState().hasSelection()) {
            controller.getListener().onMutationStarting();
            controller.getDragController().beginDrag(
                    controller.getState().getSelectedModels(),
                    controller.designX(controller.getDownScreenX()),
                    controller.designY(controller.getDownScreenY()));
            controller.getDragController().updateDrag(
                    controller.designX(event.getX()),
                    controller.designY(event.getY()));
            controller.getListener().onDocumentChanged();
            controller.invalidate();
            return true;
        }
        return false;
    }

    @Override
    public boolean onUp(GestureController controller, MotionEvent event) {
        if (marqueeActive) {
            marqueeActive = false;
            RectF rect = controller.getState().getMarqueeRect();
            controller.getState().setMarqueeRect(null);
            if (rect != null && (rect.width() > 4 || rect.height() > 4)) {
                new MultiSelectionManager(controller.getState())
                        .selectInRect(controller.getState().getRoot(), rect);
                controller.getListener().onSelectionChanged();
            } else {
                controller.getState().clearSelection();
                controller.getListener().onSelectionChanged();
                controller.getListener().onEmptyCanvasTapped();
            }
            controller.invalidate();
            return true;
        }
        return true;
    }

    @Override
    public boolean onLongPress(GestureController controller, MotionEvent event) {
        return false;
    }
}
