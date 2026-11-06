package com.nobi.appbuilder.ui.codeeditor;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.codeeditor.EditorDocument;
import com.nobi.appbuilder.codeeditor.diagnostics.CodeDiagnostic;
import com.nobi.appbuilder.codeeditor.diagnostics.DiagnosticAdapter;

import java.util.List;

/** Status bar (line/col, language, save state) + diagnostics list. */
public class CodeBottomPanelController {

    public interface BottomListener {
        void onDiagnosticSelected(CodeDiagnostic diagnostic);
    }

    private final TextView statusView;
    private final TextView saveStateView;
    private final RecyclerView diagnosticsView;
    private final DiagnosticAdapter adapter;

    public CodeBottomPanelController(View root, BottomListener listener) {
        statusView = root.findViewById(R.id.text_code_status);
        saveStateView = root.findViewById(R.id.text_code_save_state);
        diagnosticsView = root.findViewById(R.id.recycler_code_diagnostics);
        diagnosticsView.setLayoutManager(new LinearLayoutManager(
                root.getContext()));
        adapter = new DiagnosticAdapter(diagnostic ->
                listener.onDiagnosticSelected(diagnostic));
        diagnosticsView.setAdapter(adapter);
    }

    public void updateStatus(EditorDocument document, int line, int column) {
        if (document == null) {
            statusView.setText("");
            return;
        }
        statusView.setText("Ln " + line + ", Col " + column + "  ·  "
                + document.getLanguage().getDisplayName());
    }

    public void setSaveState(String state) {
        saveStateView.setText(state);
    }

    public void showDiagnostics(List<CodeDiagnostic> diagnostics) {
        adapter.setItems(diagnostics);
        diagnosticsView.setVisibility(
                diagnostics.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
