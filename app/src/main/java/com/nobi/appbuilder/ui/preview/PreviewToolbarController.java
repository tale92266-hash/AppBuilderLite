package com.nobi.appbuilder.ui.preview;

import com.google.android.material.appbar.MaterialToolbar;
import com.nobi.appbuilder.R;

/**
 * Owns the preview top toolbar: back navigation, the current screen
 * name as title, and the restart / theme / close menu.
 */
public class PreviewToolbarController {

    public interface ToolbarListener {
        void onBack();

        void onRestart();

        void onTheme();

        void onClose();
    }

    private final MaterialToolbar toolbar;
    private final ToolbarListener listener;

    public PreviewToolbarController(MaterialToolbar toolbar,
                                    ToolbarListener listener) {
        this.toolbar = toolbar;
        this.listener = listener;
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> listener.onBack());
        toolbar.inflateMenu(R.menu.menu_preview);
        toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_restart) {
                listener.onRestart();
                return true;
            }
            if (id == R.id.action_theme) {
                listener.onTheme();
                return true;
            }
            if (id == R.id.action_close) {
                listener.onClose();
                return true;
            }
            return false;
        });
    }

    public void setTitle(String title) {
        toolbar.setTitle(title);
    }
}
