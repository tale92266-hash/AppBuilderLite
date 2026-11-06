package com.nobi.appbuilder.history;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The undo/redo stacks. Holds commands only (never full-document
 * copies): each command stores just the fields it needs to restore
 * the previous state. The redo stack is cleared whenever a new
 * command is pushed, and the undo stack is trimmed to maxSize.
 */
public class CommandHistory {

    public static final int DEFAULT_MAX_SIZE = 100;

    private final Deque<EditorCommand> undoStack = new ArrayDeque<>();
    private final Deque<EditorCommand> redoStack = new ArrayDeque<>();
    private int maxSize = DEFAULT_MAX_SIZE;

    public void setMaxSize(int maxSize) {
        this.maxSize = Math.max(1, maxSize);
        trim();
    }

    public int getMaxSize() {
        return maxSize;
    }

    /** Pushes a command that has already executed. Clears redo. */
    public void push(EditorCommand command) {
        undoStack.push(command);
        redoStack.clear();
        trim();
    }

    public EditorCommand popUndo() {
        return undoStack.isEmpty() ? null : undoStack.pop();
    }

    public EditorCommand popRedo() {
        return redoStack.isEmpty() ? null : redoStack.pop();
    }

    public void pushRedo(EditorCommand command) {
        redoStack.push(command);
    }

    public void pushUndo(EditorCommand command) {
        undoStack.push(command);
        trim();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public int getUndoSize() {
        return undoStack.size();
    }

    public int getRedoSize() {
        return redoStack.size();
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    private void trim() {
        while (undoStack.size() > maxSize) {
            undoStack.removeLast();
        }
    }
}
