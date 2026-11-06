package com.nobi.appbuilder.ui.codeeditor;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.codeeditor.CodeEditorController;
import com.nobi.appbuilder.codeeditor.CodeEditorView;
import com.nobi.appbuilder.codeeditor.EditorAutoSaveManager;
import com.nobi.appbuilder.codeeditor.EditorDocument;
import com.nobi.appbuilder.codeeditor.EditorFileManager;
import com.nobi.appbuilder.codeeditor.EditorHistoryManager;
import com.nobi.appbuilder.codeeditor.EditorSettings;
import com.nobi.appbuilder.codeeditor.completion.CodeCompletionManager;
import com.nobi.appbuilder.codeeditor.completion.CompletionAdapter;
import com.nobi.appbuilder.codeeditor.completion.CompletionItem;
import com.nobi.appbuilder.codeeditor.diagnostics.CodeDiagnostic;
import com.nobi.appbuilder.codeeditor.diagnostics.CodeDiagnosticManager;
import com.nobi.appbuilder.codeeditor.search.SearchResult;
import com.nobi.appbuilder.codeproject.CodeFileOperations;
import com.nobi.appbuilder.codeproject.CodeProjectManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Professional code editor activity: tabs, file explorer, completion,
 * find/replace, diagnostics, autosave and session restore.
 */
public class CodeEditorActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";

    private String projectId;
    private CodeProjectManager projectManager;
    private EditorFileManager fileManager;
    private EditorSettings settings;
    private EditorHistoryManager historyManager;
    private CodeEditorView editorView;
    private CodeEditorController controller;
    private CodeEditorToolbarController toolbarController;
    private CodeFileExplorerController explorerController;
    private CodeTabController tabController;
    private CodeBottomPanelController bottomController;
    private CodeSearchPanelController searchController;
    private RecyclerView completionView;
    private CompletionAdapter completionAdapter;
    private View explorerPanel;
    private View searchPanel;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private com.nobi.appbuilder.sync.SynchronizationManager syncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_editor);
        projectId = getIntent().getStringExtra(EXTRA_PROJECT_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.editor_no_project,
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        projectManager = new CodeProjectManager(this, projectId);
        File workspace = projectManager.getWorkspaceDir();
        fileManager = new EditorFileManager(workspace);
        settings = new EditorSettings(this);
        historyManager = new EditorHistoryManager(this);
        editorView = findViewById(R.id.editor_code_view);
        controller = new CodeEditorController(editorView, fileManager,
                settings);
        controller.setListener(new CodeEditorController.ControllerListener() {
            @Override
            public void onDocumentChanged(EditorDocument document) {
                refreshTabs();
                updateStatus();
                runDiagnostics(document);
                searchController.setDocumentText(
                        document == null ? "" : document.getText());
            }

            @Override
            public void onSaveState(EditorAutoSaveManager.SaveState state) {
                bottomController.setSaveState(state.name());
                // Phase 11: a saved layout XML is a code-side change.
                if (state == EditorAutoSaveManager.SaveState.SAVED) {
                    notifyCodeChange();
                }
            }

            @Override
            public void onDirtyChanged(boolean dirty) {
                refreshTabs();
                toolbarController.setUndoRedoEnabled(controller.canUndo(),
                        controller.canRedo());
            }
        });
        toolbarController = new CodeEditorToolbarController(
                findViewById(R.id.layout_code_toolbar),
                new CodeEditorToolbarController.ToolbarListener() {
                    @Override
                    public void onSave() {
                        if (controller.saveActive()) {
                            bottomController.setSaveState("Saved");
                            Toast.makeText(CodeEditorActivity.this,
                                    R.string.editor_saved,
                                    Toast.LENGTH_SHORT).show();
                            notifyCodeChange();
                        }
                    }

                    @Override
                    public void onUndo() {
                        controller.undo();
                    }

                    @Override
                    public void onRedo() {
                        controller.redo();
                    }

                    @Override
                    public void onFormat() {
                        if (controller.formatActive()) {
                            Toast.makeText(CodeEditorActivity.this,
                                    R.string.editor_formatted,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onSearch() {
                        searchPanel.setVisibility(
                                searchPanel.getVisibility() == View.VISIBLE
                                        ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onToggleExplorer() {
                        explorerPanel.setVisibility(
                                explorerPanel.getVisibility() == View.VISIBLE
                                        ? View.GONE : View.VISIBLE);
                    }

                    @Override
                    public void onSettings() {
                        new CodeSettingsController(CodeEditorActivity.this,
                                settings, () -> controller.applySettings())
                                .showDialog();
                    }
                });
        explorerPanel = findViewById(R.id.layout_code_explorer);
        explorerController = new CodeFileExplorerController(explorerPanel,
                workspace, new CodeFileExplorerController.ExplorerListener() {
                    @Override
                    public void onOpenFile(File file) {
                        openFileWithConflictCheck(file);
                    }

                    @Override
                    public void onDeleteFile(File file) {
                        confirmDelete(file);
                    }
                });
        tabController = new CodeTabController(
                findViewById(R.id.layout_code_tabs),
                new CodeTabController.TabListener() {
                    @Override
                    public void onSelectTab(int index) {
                        controller.getSession().setActive(index);
                    }

                    @Override
                    public void onCloseTab(int index) {
                        closeTab(index);
                    }
                });
        bottomController = new CodeBottomPanelController(
                findViewById(R.id.layout_code_bottom),
                diagnostic -> controller.goToLine(diagnostic.getLine()));
        searchPanel = findViewById(R.id.layout_code_search);
        searchController = new CodeSearchPanelController(searchPanel,
                controller.getFindReplace(),
                new CodeSearchPanelController.SearchListener() {
                    @Override
                    public void onMatchChanged() {
                        highlightCurrentMatch();
                    }

                    @Override
                    public void onReplaceCurrent() {
                        EditorDocument document =
                                controller.getActiveDocument();
                        if (document == null) {
                            return;
                        }
                        String replaced = controller.getFindReplace()
                                .replaceCurrent(editorView.getText().toString(),
                                        searchController.getReplaceText());
                        editorView.setText(replaced);
                        searchController.setDocumentText(replaced);
                    }

                    @Override
                    public void onReplaceAll() {
                        EditorDocument document =
                                controller.getActiveDocument();
                        if (document == null) {
                            return;
                        }
                        String replaced = controller.getFindReplace()
                                .replaceAll(editorView.getText().toString(),
                                        searchController.getReplaceText());
                        editorView.setText(replaced);
                        searchController.setDocumentText(replaced);
                    }
                });
        completionView = findViewById(R.id.recycler_code_completion);
        completionView.setLayoutManager(new LinearLayoutManager(this));
        completionAdapter = new CompletionAdapter(item -> {
            controller.insertCompletion(item);
            completionView.setVisibility(View.GONE);
            editorView.requestFocus();
        });
        completionView.setAdapter(completionAdapter);
        editorView.setSelectionListener((start, end) -> updateStatus());
        // Phase 11: sync manager + code refresh listener.
        syncManager = new com.nobi.appbuilder.sync.SynchronizationManager(
                this, projectId);
        com.nobi.appbuilder.sync.reload.CodeEditorRefreshManager.register(
                request -> {
                    EditorDocument active = controller.getActiveDocument();
                    if (active != null && isLayoutXml(active.getFile())) {
                        if (controller.reloadActiveFromDisk()) {
                            explorerController.refresh();
                            runDiagnostics(active);
                        }
                    }
                });
        editorView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                updateCompletion();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        loadWorkspace();
    }

    private boolean isLayoutXml(File file) {
        return file != null && file.getName().endsWith(".xml")
                && file.getAbsolutePath().contains(
                "app/src/main/res/layout/");
    }

    /** Phase 11: tells the sync engine the active layout XML changed. */
    private void notifyCodeChange() {
        if (syncManager == null) {
            return;
        }
        EditorDocument saved = controller.getActiveDocument();
        if (saved != null && isLayoutXml(saved.getFile())) {
            String screenId = syncManager.screenIdForLayoutFile(
                    saved.getFile().getName());
            if (screenId != null) {
                syncManager.onCodeChange(screenId,
                        saved.getFile().getAbsolutePath());
            }
        }
    }

    private void loadWorkspace() {
        executor.execute(() -> {
            File workspace = projectManager.getWorkspaceDir();
            boolean empty = new File(workspace, "app").exists() == false
                    && new File(workspace, "settings.gradle").exists() == false;
            List<String> conflicts = new ArrayList<>();
            if (empty) {
                conflicts = projectManager.syncFromGeneration();
            }
            List<String> syncConflicts = conflicts;
            mainHandler.post(() -> {
                explorerController.refresh();
                restoreSession();
                if (!syncConflicts.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.editor_sync_conflicts)
                            .setMessage(getString(
                                    R.string.editor_sync_conflicts_text,
                                    syncConflicts.size()))
                            .setPositiveButton(android.R.string.ok, null)
                            .show();
                }
            });
        });
    }

    private void restoreSession() {
        EditorHistoryManager.SavedSession saved =
                historyManager.loadSession(projectId);
        if (saved == null || saved.tabs == null || saved.tabs.isEmpty()) {
            return;
        }
        for (EditorHistoryManager.TabState tab : saved.tabs) {
            File file = new File(tab.path);
            if (file.exists()) {
                EditorDocument document = controller.openFile(file);
                if (document != null && tab.cursorOffset > 0) {
                    editorView.setSelection(Math.min(tab.cursorOffset,
                            editorView.length()));
                }
            }
        }
        if (saved.activeIndex >= 0
                && saved.activeIndex < controller.getSession()
                .getDocuments().size()) {
            controller.getSession().setActive(saved.activeIndex);
        }
    }

    private void openFileWithConflictCheck(File file) {
        EditorDocument existing = controller.getSession().findByFile(file);
        if (existing != null && existing.isExternallyModified()
                && existing.isDirty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.editor_external_change)
                    .setMessage(R.string.editor_external_change_text)
                    .setPositiveButton(R.string.editor_reload,
                            (dialog, which) -> controller.openFile(file))
                    .setNegativeButton(R.string.editor_keep_mine, null)
                    .show();
            return;
        }
        controller.openFile(file);
    }

    private void closeTab(int index) {
        List<EditorDocument> documents =
                controller.getSession().getDocuments();
        if (index < 0 || index >= documents.size()) {
            return;
        }
        EditorDocument document = documents.get(index);
        if (document.isDirty()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.editor_unsaved_changes)
                    .setMessage(getString(R.string.editor_unsaved_text,
                            document.getFileName()))
                    .setPositiveButton(R.string.editor_save_and_close,
                            (dialog, which) -> {
                                controller.getSession().setActive(index);
                                controller.saveActive();
                                controller.getSession().close(index);
                                refreshTabs();
                            })
                    .setNegativeButton(R.string.editor_discard,
                            (dialog, which) -> {
                                controller.getSession().close(index);
                                refreshTabs();
                            })
                    .setNeutralButton(android.R.string.cancel, null)
                    .show();
            return;
        }
        controller.getSession().close(index);
        refreshTabs();
    }

    private void confirmDelete(File file) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.editor_delete_file)
                .setMessage(getString(R.string.editor_delete_text,
                        file.getName()))
                .setPositiveButton(R.string.editor_delete, (dialog, which) -> {
                    if (new CodeFileOperations(
                            projectManager.getWorkspaceDir()).delete(file)) {
                        explorerController.refresh();
                    } else {
                        Toast.makeText(this, R.string.editor_delete_blocked,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void refreshTabs() {
        tabController.setTabs(controller.getSession().getDocuments(),
                controller.getSession().getActiveIndex());
        toolbarController.setUndoRedoEnabled(controller.canUndo(),
                controller.canRedo());
    }

    private void updateStatus() {
        EditorDocument document = controller.getActiveDocument();
        if (document == null) {
            bottomController.updateStatus(null, 1, 1);
            return;
        }
        int offset = Math.max(0, editorView.getSelectionStart());
        int line = 1;
        int column = 1;
        String text = editorView.getText() == null ? ""
                : editorView.getText().toString();
        int bound = Math.min(offset, text.length());
        for (int i = 0; i < bound; i++) {
            if (text.charAt(i) == '\n') {
                line++;
                column = 1;
            } else {
                column++;
            }
        }
        bottomController.updateStatus(document, line, column);
    }

    private void runDiagnostics(EditorDocument document) {
        if (document == null) {
            bottomController.showDiagnostics(new ArrayList<>());
            return;
        }
        List<CodeDiagnostic> diagnostics =
                new CodeDiagnosticManager().diagnose(document);
        bottomController.showDiagnostics(diagnostics);
    }

    private void updateCompletion() {
        if (!settings.isAutoCompletion()) {
            completionView.setVisibility(View.GONE);
            return;
        }
        EditorDocument document = controller.getActiveDocument();
        if (document == null) {
            completionView.setVisibility(View.GONE);
            return;
        }
        int offset = editorView.getSelectionStart();
        if (offset < 0) {
            completionView.setVisibility(View.GONE);
            return;
        }
        CodeCompletionManager manager = controller.getCompletionManager();
        List<CompletionItem> items = manager.complete(
                editorView.getText().toString(), offset,
                document.getLanguage());
        if (items.isEmpty()) {
            completionView.setVisibility(View.GONE);
            return;
        }
        completionAdapter.setItems(items);
        completionView.setVisibility(View.VISIBLE);
    }

    private void highlightCurrentMatch() {
        SearchResult result = controller.getFindReplace().current();
        if (result == null) {
            return;
        }
        editorView.requestFocus();
        editorView.setSelection(result.getStart(), result.getEnd());
    }

    @Override
    public void onBackPressed() {
        if (searchPanel.getVisibility() == View.VISIBLE) {
            searchPanel.setVisibility(View.GONE);
            return;
        }
        if (controller.getSession().hasUnsavedChanges()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.editor_unsaved_changes)
                    .setMessage(R.string.editor_unsaved_exit_text)
                    .setPositiveButton(R.string.editor_save_and_close,
                            (dialog, which) -> {
                                controller.saveAll();
                                finish();
                            })
                    .setNegativeButton(R.string.editor_discard,
                            (dialog, which) -> finish())
                    .setNeutralButton(android.R.string.cancel, null)
                    .show();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        historyManager.saveSession(projectId, controller.getSession());
    }

    @Override
    protected void onDestroy() {
        controller.release();
        executor.shutdownNow();
        if (syncManager != null) {
            syncManager.release();
        }
        com.nobi.appbuilder.sync.reload.CodeEditorRefreshManager.unregister();
        super.onDestroy();
    }
}
