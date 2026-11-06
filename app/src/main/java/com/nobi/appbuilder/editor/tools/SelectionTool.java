package com.nobi.appbuilder.editor.tools;

import android.view.MotionEvent;

import com.nobi.appbuilder.editor.GestureController;
import com.nobi.appbuilder.model.ComponentModel;

/**
 * Default tool: tap to select, drag to move, handles to resize.
 * A drag only starts after the touch slop is exceeded, so a tap never
 * accidentally moves a component.
 */
public class SelectionTool implements GestureController.Tool {

    @Override
    public String getName() {
        return "select";
    }

    @Override
    public boolean onDown(GestureController controller, MotionEvent event) {
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
        controller.getState().clearSelection();
        controller.getListener().onSelectionChanged();
        controller.getListener().onEmptyCanvasTapped();
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
        return true;
    }

    @Override
    public boolean onLongPress(GestureController controller, MotionEvent event) {
        return false;
    }
}
