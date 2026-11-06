package com.nobi.appbuilder.clipboard;

import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.hierarchy.HierarchyValidator;
import com.nobi.appbuilder.history.AddComponentCommand;
import com.nobi.appbuilder.history.EditorCommand;
import com.nobi.appbuilder.model.ComponentModel;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns clipboard data into paste commands. Each paste re-clones from
 * the stored data (fresh ids every time), offsets the copies slightly
 * from the originals, keeps them inside the target parent's bounds,
 * and returns AddComponentCommands so the paste is one undoable step.
 * Paste targets the deepest valid container under the drop point, or
 * the screen root when no container is given.
 */
public class PasteController {

    public static final int PASTE_OFFSET_DP = 16;

    public static class PasteResult {
        public final List<EditorCommand> commands = new ArrayList<>();
        public final List<ComponentModel> pasted = new ArrayList<>();
        public String error;
    }

    public PasteResult preparePaste(ClipboardData data, ComponentModel root,
                                    ComponentModel targetParent, int offsetDp) {
        PasteResult result = new PasteResult();
        if (data == null || data.isEmpty()) {
            result.error = "Nothing copied yet.";
            return result;
        }
        if (root == null) {
            result.error = "No screen open.";
            return result;
        }
        ComponentModel parent = targetParent != null ? targetParent : root;
        RectF screenBounds = new RectF(0, 0, 360, 640);
        RectF parentBounds = ComponentHitTester.resolveBoundsFor(
                root, parent.getId(), screenBounds);
        if (parentBounds == null) {
            parentBounds = screenBounds;
        }
        for (ComponentModel stored : data.getModels()) {
            String error = HierarchyValidator.validateParentAccepts(
                    parent, stored.getType());
            if (error != null) {
                result.error = error;
                return result;
            }
            ComponentModel copy = ComponentCloner.clone(stored);
            copy.setX(copy.getX() + offsetDp);
            copy.setY(copy.getY() + offsetDp);
            clampInside(copy, parentBounds);
            result.commands.add(new AddComponentCommand(parent, copy, -1));
            result.pasted.add(copy);
        }
        return result;
    }

    /** Keeps a pasted component inside its parent's bounds. */
    private void clampInside(ComponentModel model, RectF parentBounds) {
        float maxX = parentBounds.right - model.getWidth();
        float maxY = parentBounds.bottom - model.getHeight();
        model.setX(Math.round(Math.max(parentBounds.left,
                Math.min(model.getX(), Math.max(parentBounds.left, maxX)))));
        model.setY(Math.round(Math.max(parentBounds.top,
                Math.min(model.getY(), Math.max(parentBounds.top, maxY)))));
    }
}
