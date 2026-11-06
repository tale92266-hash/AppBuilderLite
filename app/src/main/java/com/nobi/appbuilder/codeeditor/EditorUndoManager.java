package com.nobi.appbuilder.codeeditor;

import java.util.ArrayDeque;
import java.util.Deque;

/** Snapshot-based undo/redo. One entry per completed edit batch;
 * consecutive keystrokes within 800 ms merge into one entry. */
public class EditorUndoManager {

    public static class Snapshot {
        public final String text;
        public final int selectionStart;
        public final int selectionEnd;

        public Snapshot(String text, int selectionStart, int selectionEnd) {
            this.text = text;
            this.selectionStart = selectionStart;
            this.selectionEnd = selectionEnd;
        }
    }

    private static final int MAX_ENTRIES = 100;
    private static final long MERGE_WINDOW_MS = 800;

    private final Deque<Snapshot> undoStack = new ArrayDeque<>();
    private final Deque<Snapshot> redoStack = new ArrayDeque<>();
    private long lastPushMs;

    public void push(String text, int selectionStart, int selectionEnd) {
        long now = System.currentTimeMillis();
        Snapshot top = undoStack.peek();
        if (top != null && top.text.equals(text)) {
            return;
        }
        if (top != null && now - lastPushMs < MERGE_WINDOW_MS
                && isSmallChange(top.text, text)) {
            // Merge: keep the older snapshot, refresh the selection.
            undoStack.pop();
            undoStack.push(new Snapshot(top.text, selectionStart,
                    selectionEnd));
            lastPushMs = now;
            return;
        }
        undoStack.push(new Snapshot(text, selectionStart, selectionEnd));
        while (undoStack.size() > MAX_ENTRIES) {
            undoStack.removeLast();
        }
        redoStack.clear();
        lastPushMs = now;
    }

    public Snapshot undo(String currentText, int selectionStart,
                         int selectionEnd) {
        if (undoStack.isEmpty()) {
            return null;
        }
        redoStack.push(new Snapshot(currentText, selectionStart,
                selectionEnd));
        return undoStack.pop();
    }

    public Snapshot redo(String currentText, int selectionStart,
                         int selectionEnd) {
        if (redoStack.isEmpty()) {
            return null;
        }
        undoStack.push(new Snapshot(currentText, selectionStart,
                selectionEnd));
        return redoStack.pop();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    private boolean isSmallChange(String oldText, String newText) {
        return Math.abs(oldText.length() - newText.length()) <= 2;
    }
}
