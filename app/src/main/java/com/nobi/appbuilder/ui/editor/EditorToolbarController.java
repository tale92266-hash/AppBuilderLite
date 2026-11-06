package com.nobi.appbuilder.ui.editor;

import android.view.MenuItem;

import com.google.android.material.appbar.MaterialToolbar;
import com.nobi.appbuilder.R;

/**
 * Owns the editor top toolbar: back navigation, title, and the
 * undo / redo / save / preview menu. Preview is Phase 6, so its item
 * stays visibly disabled until then.
 */
public class EditorToolbarController {

    public interface ToolbarListener {
        void onBack();

        void onUndo();

        void onRedo();

        void onSave();

        void onCopy();

        void onPaste();

        void onDuplicate();

        void onScreens();
    }

    private final MaterialToolbar toolbar;
    private final ToolbarListener listener;

    public EditorToolbarController(MaterialToolbar toolbar, ToolbarListener listener) {
        this.toolbar = toolbar;
        this.listener = listener;
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> listener.onBack());
        toolbar.inflateMenu(R.menu.menu_editor);
        MenuItem preview = toolbar.getMenu().findItem(R.id.action_preview);
        if (preview != null) {
            preview.setEnabled(false);
        }
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_undo) {
                listener.onUndo();
                return true;
            }
            if (id == R.id.action_redo) {
                listener.onRedo();
                return true;
            }
            if (id == R.id.action_save) {
                listener.onSave();
                return true;
            }
            if (id == R.id.action_copy) {
                listener.onCopy();
                return true;
            }
            if (id == R.id.action_paste) {
                listener.onPaste();
                return true;
            }
            if (id == R.id.action_duplicate) {
                listener.onDuplicate();
                return true;
            }
            if (id == R.id.action_screens) {
                listener.onScreens();
                return true;
            }
            return false;
        });
    }

    public void setTitle(String title) {
        toolbar.setTitle(title);
    }

    public void setUndoEnabled(boolean enabled) {
        MenuItem item = toolbar.getMenu().findItem(R.id.action_undo);
        if (item != null) {
            item.setEnabled(enabled);
        }
    }

    public void setRedoEnabled(boolean enabled) {
        MenuItem item = toolbar.getMenu().findItem(R.id.action_redo);
        if (item != null) {
            item.setEnabled(enabled);
        }
    }

    public void setCopyEnabled(boolean enabled) {
        MenuItem item = toolbar.getMenu().findItem(R.id.action_copy);
        if (item != null) {
            item.setEnabled(enabled);
        }
    }

    public void setPasteEnabled(boolean enabled) {
        MenuItem item = toolbar.getMenu().findItem(R.id.action_paste);
        if (item != null) {
            item.setEnabled(enabled);
        }
    }

    public void setDuplicateEnabled(boolean enabled) {
        MenuItem item = toolbar.getMenu().findItem(R.id.action_duplicate);
        if (item != null) {
            item.setEnabled(enabled);
        }
    }

    /** Save status shown as the toolbar subtitle (Phase 5 autosave). */
    public void setSaveStatus(String status) {
        toolbar.setSubtitle(status);
    }
}
