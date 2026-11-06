package com.nobi.appbuilder.history;

/**
 * One reversible editing operation. Commands mutate the component model
 * tree directly (never the database); the CommandManager owns history,
 * and the activity owns saving and UI refresh after each command.
 * A command must be atomic: validate everything in canExecute() so
 * execute() cannot fail halfway through the model.
 */
public interface EditorCommand {

    /** Applies the operation. Must be safe to call again after undo(). */
    void execute();

    /** Restores the exact state from before execute(). */
    void undo();

    /** Short human-readable label, e.g. "Move Button". */
    String getDescription();

    /** True when the command is valid and may run. Defaults to true. */
    default boolean canExecute() {
        return true;
    }
}
