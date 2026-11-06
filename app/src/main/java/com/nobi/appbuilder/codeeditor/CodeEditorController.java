package com.nobi.appbuilder.codeeditor;

import android.text.Editable;
import android.text.TextWatcher;

import com.nobi.appbuilder.codeeditor.completion.CodeCompletionManager;
import com.nobi.appbuilder.codeeditor.completion.CompletionItem;
import com.nobi.appbuilder.codeeditor.formatting.CodeFormatter;
import com.nobi.appbuilder.codeeditor.formatting.FormattingResult;
import com.nobi.appbuilder.codeeditor.formatting.JavaCodeFormatter;
import com.nobi.appbuilder.codeeditor.formatting.XmlCodeFormatter;
import com.nobi.appbuilder.codeeditor.navigation.CodeNavigationManager;
import com.nobi.appbuilder.codeeditor.search.FindReplaceController;
import com.nobi.appbuilder.codeeditor.syntax.JavaSyntaxHighlighter;
import com.nobi.appbuilder.codeeditor.syntax.SyntaxColorResolver;
import com.nobi.appbuilder.codeeditor.syntax.SyntaxHighlighter;
import com.nobi.appbuilder.codeeditor.syntax.XmlSyntaxHighlighter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Orchestrates one editing session: documents, undo, autosave,
 * completion context, formatting and find/replace. The view never
 * writes a file directly — every save goes through the file manager.
 */
public class CodeEditorController {

    public interface ControllerListener {
        void onDocumentChanged(EditorDocument document);

        void onSaveState(EditorAutoSaveManager.SaveState state);

        void onDirtyChanged(boolean dirty);
    }

    private final CodeEditorView view;
    private final EditorFileManager fileManager;
    private final EditorSettings settings;
    private final EditorSession session = new EditorSession();
    private final EditorUndoManager undoManager = new EditorUndoManager();
    private final EditorAutoSaveManager autoSaveManager =
            new EditorAutoSaveManager();
    private final FindReplaceController findReplace =
            new FindReplaceController();
    private final CodeCompletionManager completionManager =
            new CodeCompletionManager();
    private final SyntaxColorResolver colorResolver;
    private final List<CodeFormatter> formatters = new ArrayList<>();
    private ControllerListener listener;
    private boolean applyingSnapshot;

    public CodeEditorController(CodeEditorView view,
                                EditorFileManager fileManager,
                                EditorSettings settings) {
        this.view = view;
        this.fileManager = fileManager;
        this.settings = settings;
        this.colorResolver = new SyntaxColorResolver(settings.getTheme());
        view.setEditorSettings(settings);
        view.setColorResolver(colorResolver);
        formatters.add(new JavaCodeFormatter());
        formatters.add(new XmlCodeFormatter());
        autoSaveManager.setListener(state -> {
            if (listener != null) {
                listener.onSaveState(state);
            }
        });
        session.addListener(new EditorSession.SessionListener() {
            @Override
            public void onActiveChanged(EditorDocument document) {
                loadIntoView(document);
                if (listener != null) {
                    listener.onDocumentChanged(document);
                }
            }

            @Override
            public void onDocumentClosed(EditorDocument document) {
            }

            @Override
            public void onDirtyChanged(EditorDocument document) {
            }
        });
        view.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (applyingSnapshot) {
                    return;
                }
                EditorDocument document = session.getActive();
                if (document == null) {
                    return;
                }
                document.setText(s.toString());
                undoManager.push(s.toString(), view.getSelectionStart(),
                        view.getSelectionEnd());
                if (settings.isAutoSave()) {
                    autoSaveManager.markDirty(() -> saveDocument(document));
                }
                if (listener != null) {
                    listener.onDirtyChanged(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        refreshCompletionContext();
    }

    public void setListener(ControllerListener listener) {
        this.listener = listener;
    }

    public EditorSession getSession() {
        return session;
    }

    public FindReplaceController getFindReplace() {
        return findReplace;
    }

    public CodeCompletionManager getCompletionManager() {
        return completionManager;
    }

    public EditorDocument getActiveDocument() {
        return session.getActive();
    }

    /** Opens a file (or focuses its tab); returns null on failure. */
    public EditorDocument openFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        EditorDocument existing = session.findByFile(file);
        if (existing != null) {
            session.setActive(session.getDocuments().indexOf(existing));
            return existing;
        }
        try {
            String text = fileManager.read(file);
            EditorDocument document = new EditorDocument(file, text);
            session.open(document);
            undoManager.clear();
            undoManager.push(text, 0, 0);
            return document;
        } catch (IOException e) {
            return null;
        }
    }

    public boolean saveActive() {
        EditorDocument document = session.getActive();
        if (document == null) {
            return false;
        }
        return saveDocument(document);
    }

    public void saveAll() {
        for (EditorDocument document : session.getDocuments()) {
            if (document.isDirty()) {
                saveDocument(document);
            }
        }
    }

    private boolean saveDocument(EditorDocument document) {
        boolean ok = fileManager.save(document.getFile(),
                document.getText());
        if (ok) {
            document.markSaved();
            if (listener != null) {
                listener.onDirtyChanged(false);
            }
        }
        return ok;
    }

    public void undo() {
        EditorDocument document = session.getActive();
        if (document == null || !undoManager.canUndo()) {
            return;
        }
        EditorUndoManager.Snapshot snapshot = undoManager.undo(
                view.getText().toString(), view.getSelectionStart(),
                view.getSelectionEnd());
        if (snapshot != null) {
            applySnapshot(snapshot);
        }
    }

    public void redo() {
        EditorDocument document = session.getActive();
        if (document == null || !undoManager.canRedo()) {
            return;
        }
        EditorUndoManager.Snapshot snapshot = undoManager.redo(
                view.getText().toString(), view.getSelectionStart(),
                view.getSelectionEnd());
        if (snapshot != null) {
            applySnapshot(snapshot);
        }
    }

    public boolean canUndo() {
        return undoManager.canUndo();
    }

    public boolean canRedo() {
        return undoManager.canRedo();
    }

    private void applySnapshot(EditorUndoManager.Snapshot snapshot) {
        applyingSnapshot = true;
        view.setText(snapshot.text);
        int start = Math.min(snapshot.selectionStart,
                snapshot.text.length());
        int end = Math.min(snapshot.selectionEnd, snapshot.text.length());
        view.setSelection(start, end);
        EditorDocument document = session.getActive();
        if (document != null) {
            document.setText(snapshot.text);
        }
        applyingSnapshot = false;
        view.highlightNow();
    }

    private void loadIntoView(EditorDocument document) {
        applyingSnapshot = true;
        view.setText(document.getText());
        SyntaxHighlighter highlighter = highlighterFor(
                document.getLanguage());
        view.setHighlighter(highlighter);
        EditorCursor cursor = document.getState().getCursor();
        int offset = Math.min(cursor.getOffset(),
                document.getText().length());
        view.setSelection(offset);
        applyingSnapshot = false;
        view.highlightNow();
    }

    private SyntaxHighlighter highlighterFor(EditorLanguage language) {
        switch (language) {
            case JAVA:
                return new JavaSyntaxHighlighter();
            case XML:
                return new XmlSyntaxHighlighter();
            default:
                return null;
        }
    }

    /** Formats the active document (undoable). Returns false when the
     * language has no formatter. */
    public boolean formatActive() {
        EditorDocument document = session.getActive();
        if (document == null) {
            return false;
        }
        for (CodeFormatter formatter : formatters) {
            if (!formatter.supports(document.getLanguage())) {
                continue;
            }
            FormattingResult result = formatter.format(document.getText(),
                    settings.getTabWidth());
            if (result.isSuccess() && result.isChanged()) {
                undoManager.push(document.getText(),
                        view.getSelectionStart(), view.getSelectionEnd());
                applySnapshot(new EditorUndoManager.Snapshot(
                        result.getText(), view.getSelectionStart(),
                        view.getSelectionEnd()));
                return true;
            }
            return result.isSuccess();
        }
        return false;
    }

    public void goToLine(int line) {
        EditorDocument document = session.getActive();
        if (document == null) {
            return;
        }
        int offset = CodeNavigationManager.offsetForLine(document, line);
        view.setSelection(offset);
        view.requestFocus();
    }

    /** Inserts a completion suggestion, replacing the current token. */
    public void insertCompletion(CompletionItem item) {
        EditorDocument document = session.getActive();
        if (document == null) {
            return;
        }
        Editable editable = view.getText();
        if (editable == null) {
            return;
        }
        int cursor = view.getSelectionStart();
        int start = cursor;
        while (start > 0) {
            char c = editable.charAt(start - 1);
            if (Character.isJavaIdentifierPart(c) || c == '.'
                    || c == ':' || c == '@' || c == '/') {
                start--;
            } else {
                break;
            }
        }
        // For R.id./@ref prefixes keep the qualifier, replace the tail.
        String token = editable.subSequence(start, cursor).toString();
        int dot = Math.max(token.lastIndexOf('.'), token.lastIndexOf('/'));
        if (dot >= 0) {
            start += dot + 1;
        }
        editable.replace(start, cursor, item.getText());
        view.setSelection(start + item.getText().length());
    }

    /** Scans the workspace for project classes and resource ids so
     * completion can suggest them. */
    public void refreshCompletionContext() {
        Set<String> classes = new HashSet<>();
        Set<String> resources = new HashSet<>();
        collectFrom(new File(fileManager.getWorkspaceRoot(),
                "app/src/main/java"), classes, resources, true);
        collectResources(new File(fileManager.getWorkspaceRoot(),
                "app/src/main/res"), resources);
        completionManager.setProjectClasses(classes);
        completionManager.setResourceIds(resources);
    }

    private void collectFrom(File dir, Set<String> classes,
                             Set<String> resources, boolean java) {
        if (dir == null || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collectFrom(file, classes, resources, java);
            } else if (file.getName().endsWith(".java")) {
                classes.add(file.getName().replace(".java", ""));
            }
        }
    }

    private void collectResources(File resDir, Set<String> resources) {
        if (resDir == null || !resDir.isDirectory()) {
            return;
        }
        File[] dirs = resDir.listFiles();
        if (dirs == null) {
            return;
        }
        for (File dir : dirs) {
            if (!dir.isDirectory()) {
                continue;
            }
            File[] files = dir.listFiles();
            if (files == null) {
                continue;
            }
            for (File file : files) {
                String name = file.getName();
                int dot = name.lastIndexOf('.');
                if (dot > 0) {
                    resources.add(name.substring(0, dot));
                }
            }
        }
    }

    /** Re-reads the active file from disk (sync wrote a new version).
     * Only used when the document is not dirty, so no user edit is
     * ever lost. Returns false when there is nothing to reload. */
    public boolean reloadActiveFromDisk() {
        EditorDocument document = session.getActive();
        if (document == null || document.isDirty()) {
            return false;
        }
        try {
            String text = fileManager.read(document.getFile());
            if (text.equals(document.getText())) {
                return false;
            }
            applyingSnapshot = true;
            document.setText(text);
            view.setText(text);
            applyingSnapshot = false;
            view.highlightNow();
            undoManager.clear();
            undoManager.push(text, view.getSelectionStart(),
                    view.getSelectionEnd());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /** Re-applies settings after the settings dialog (theme, font). */
    public void applySettings() {
        colorResolver.setTheme(settings.getTheme());
        view.setEditorSettings(settings);
        view.setColorResolver(colorResolver);
        view.highlightNow();
    }

    public void release() {
        autoSaveManager.cancelPending();
    }
}
