package com.nobi.appbuilder.history;

import java.util.ArrayList;
import java.util.List;

/**
 * A group of commands that undo/redo as one step (e.g. duplicating
 * several components, or a multi-part paste). Commands run in order
 * on execute() and in reverse order on undo().
 */
public class CommandTransaction implements EditorCommand {

    private final String description;
    private final List<EditorCommand> commands = new ArrayList<>();

    public CommandTransaction(String description) {
        this.description = description;
    }

    public void add(EditorCommand command) {
        commands.add(command);
    }

    public boolean isEmpty() {
        return commands.isEmpty();
    }

    public int size() {
        return commands.size();
    }

    @Override
    public void execute() {
        for (EditorCommand command : commands) {
            command.execute();
        }
    }

    @Override
    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean canExecute() {
        if (commands.isEmpty()) {
            return false;
        }
        for (EditorCommand command : commands) {
            if (!command.canExecute()) {
                return false;
            }
        }
        return true;
    }
}
