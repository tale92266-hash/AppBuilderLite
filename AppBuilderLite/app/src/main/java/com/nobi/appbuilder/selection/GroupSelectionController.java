package com.nobi.appbuilder.selection;

import com.nobi.appbuilder.history.EditorCommand;
import com.nobi.appbuilder.history.GroupComponentsCommand;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.List;

/**
 * Entry points for group / ungroup from the selection toolbar.
 * Validation lives in GroupComponentsCommand; this class only picks
 * the command (or explains why grouping is refused).
 */
public class GroupSelectionController {

    public String canGroup(ComponentModel root, List<ComponentModel> models,
                           String containerType) {
        GroupComponentsCommand command =
                new GroupComponentsCommand(root, models, containerType);
        if (command.canExecute()) {
            return null;
        }
        return command.getValidationError();
    }

    public EditorCommand groupCommand(ComponentModel root,
                                      List<ComponentModel> models,
                                      String containerType) {
        GroupComponentsCommand command =
                new GroupComponentsCommand(root, models, containerType);
        return command.canExecute() ? command : null;
    }

    public EditorCommand ungroupCommand(ComponentModel root,
                                        ComponentModel container) {
        GroupComponentsCommand.UngroupCommand command =
                new GroupComponentsCommand.UngroupCommand(root, container);
        return command.canExecute() ? command : null;
    }

    public String[] getContainerTypes() {
        return GroupComponentsCommand.GROUP_CONTAINER_TYPES;
    }
}
