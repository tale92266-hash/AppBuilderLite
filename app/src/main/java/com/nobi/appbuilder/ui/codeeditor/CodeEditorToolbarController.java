package com.nobi.appbuilder.ui.codeeditor;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nobi.appbuilder.R;

/** Toolbar actions for the code editor. */
public class CodeEditorToolbarController {

    public interface ToolbarListener {
        void onSave();
        void onUndo();
        void onRedo();
        void onFormat();
        void onSearch();
        void onToggleExplorer();
        void onSettings();
    }

    private final TextView titleView;
    private final ImageButton undoButton;
    private final ImageButton redoButton;

    public CodeEditorToolbarController(View root, ToolbarListener listener) {
        titleView = root.findViewById(R.id.text_code_title);
        undoButton = root.findViewById(R.id.btn_code_undo);
        redoButton = root.findViewById(R.id.btn_code_redo);
        root.findViewById(R.id.btn_code_save).setOnClickListener(
                v -> listener.onSave());
        undoButton.setOnClickListener(v -> listener.onUndo());
        redoButton.setOnClickListener(v -> listener.onRedo());
        root.findViewById(R.id.btn_code_format).setOnClickListener(
                v -> listener.onFormat());
        root.findViewById(R.id.btn_code_search).setOnClickListener(
                v -> listener.onSearch());
        root.findViewById(R.id.btn_code_explorer).setOnClickListener(
                v -> listener.onToggleExplorer());
        root.findViewById(R.id.btn_code_settings).setOnClickListener(
                v -> listener.onSettings());
    }

    public void setTitle(String title) {
        titleView.setText(title);
    }

    public void setUndoRedoEnabled(boolean canUndo, boolean canRedo) {
        undoButton.setEnabled(canUndo);
        redoButton.setEnabled(canRedo);
        undoButton.setAlpha(canUndo ? 1f : 0.4f);
        redoButton.setAlpha(canRedo ? 1f : 0.4f);
    }
}
