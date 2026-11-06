package com.nobi.appbuilder.history;

/**
 * Central undo/redo engine (Phase 5, replaces the Phase 3 snapshot
 * stack). Every editing operation goes through execute(); dragging
 * produces exactly one command when the drag completes, because the
 * command is built at commit time, not during the movement.
 */
public class CommandManager {

    /** Runs a command on behalf of a view/controller. */
    public interface CommandRunner {
        boolean run(EditorCommand command);
    }

    public interface HistoryListener {
        void onHistoryChanged(boolean canUndo, boolean canRedo);

        void onCommandFailed(EditorCommand command, String reason);
    }

    private final CommandHistory history = new CommandHistory();
    private HistoryListener listener;
    private boolean inTransaction;
    private CommandTransaction transaction;

    public void setListener(HistoryListener listener) {
        this.listener = listener;
    }

    public void setMaxHistorySize(int maxSize) {
        history.setMaxSize(maxSize);
    }

    public int getMaxHistorySize() {
        return history.getMaxSize();
    }

    public boolean canUndo() {
        return history.canUndo();
    }

    public boolean canRedo() {
        return history.canRedo();
    }

    /**
     * Runs a command and records it. Inside a transaction the command
     * runs immediately but is recorded only when the transaction ends.
     * Returns false (and records nothing) when the command cannot run.
     */
    public boolean execute(EditorCommand command) {
        if (command == null || !command.canExecute()) {
            fail(command, "This operation is not valid here.");
            return false;
        }
        try {
            command.execute();
        } catch (Exception e) {
            fail(command, "Operation failed: " + e.getMessage());
            return false;
        }
        if (inTransaction) {
            transaction.add(command);
        } else {
            history.push(command);
        }
        notifyChanged();
        return true;
    }

    public boolean undo() {
        EditorCommand command = history.popUndo();
        if (command == null) {
            return false;
        }
        try {
            command.undo();
        } catch (Exception e) {
            fail(command, "Undo failed: " + e.getMessage());
            return false;
        }
        history.pushRedo(command);
        notifyChanged();
        return true;
    }

    public boolean redo() {
        EditorCommand command = history.popRedo();
        if (command == null) {
            return false;
        }
        try {
            command.execute();
        } catch (Exception e) {
            fail(command, "Redo failed: " + e.getMessage());
            return false;
        }
        history.pushUndo(command);
        notifyChanged();
        return true;
    }

    /** Starts grouping commands into one undoable step. */
    public void beginTransaction(String description) {
        if (inTransaction) {
            return;
        }
        inTransaction = true;
        transaction = new CommandTransaction(description);
    }

    /** Ends grouping; the collected commands become one history entry. */
    public void endTransaction() {
        if (!inTransaction) {
            return;
        }
        inTransaction = false;
        CommandTransaction finished = transaction;
        transaction = null;
        if (!finished.isEmpty()) {
            history.push(finished);
            notifyChanged();
        }
    }

    public boolean isInTransaction() {
        return inTransaction;
    }

    /** Clears all history (switching projects or screens). */
    public void clear() {
        history.clear();
        inTransaction = false;
        transaction = null;
        notifyChanged();
    }

    private void fail(EditorCommand command, String reason) {
        if (listener != null) {
            listener.onCommandFailed(command, reason);
        }
    }

    private void notifyChanged() {
        if (listener != null) {
            listener.onHistoryChanged(history.canUndo(), history.canRedo());
        }
    }
}
