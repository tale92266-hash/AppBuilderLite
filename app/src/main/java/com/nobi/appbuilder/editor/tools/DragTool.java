package com.nobi.appbuilder.editor.tools;

import android.view.MotionEvent;

import com.nobi.appbuilder.editor.GestureController;
import com.nobi.appbuilder.model.ComponentModel;

/**
 * Tap-to-add tool, armed from the component library: the next canvas
 * tap drops a component of the armed type at that point, selects it,
 * then hands control back to the selection tool. Real drag-and-drop
 * from the library uses the Android drag framework on the canvas View.
 */
public class DragTool implements GestureController.Tool {

    private final String pendingType;

    public DragTool(String pendingType) {
        this.pendingType = pendingType;
    }

    public String getPendingType() {
        return pendingType;
    }

    @Override
    public String getName() {
        return "drag:" + pendingType;
    }

    @Override
    public boolean onDown(GestureController controller, MotionEvent event) {
        controller.getListener().onMutationStarting();
        ComponentModel created = controller.getDragController().dropNewComponent(
                pendingType,
                controller.designX(event.getX()),
                controller.designY(event.getY()));
        controller.setTool(new SelectionTool());
        if (created != null) {
            controller.getState().select(created.getId());
            controller.getListener().onSelectionChanged();
            controller.getListener().onDocumentCommitted();
        }
        controller.invalidate();
        return true;
    }

    @Override
    public boolean onMove(GestureController controller, MotionEvent event) {
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
