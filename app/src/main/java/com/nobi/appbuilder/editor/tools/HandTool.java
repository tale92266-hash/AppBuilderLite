package com.nobi.appbuilder.editor.tools;

import android.view.MotionEvent;

import com.nobi.appbuilder.editor.GestureController;

/**
 * Pan tool: dragging with one finger pans the canvas; pinch zoom still
 * works. Selection is untouched while panning.
 */
public class HandTool implements GestureController.Tool {

    @Override
    public String getName() {
        return "hand";
    }

    @Override
    public boolean onDown(GestureController controller, MotionEvent event) {
        controller.setLastPan(event.getX(), event.getY());
        return true;
    }

    @Override
    public boolean onMove(GestureController controller, MotionEvent event) {
        float dx = event.getX() - controller.getLastPanX();
        float dy = event.getY() - controller.getLastPanY();
        if (dx != 0 || dy != 0) {
            controller.getViewport().panBy(dx, dy);
            controller.setLastPan(event.getX(), event.getY());
            controller.invalidate();
        }
        return true;
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
