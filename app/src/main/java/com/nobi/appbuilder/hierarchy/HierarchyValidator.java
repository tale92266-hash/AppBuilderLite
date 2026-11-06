package com.nobi.appbuilder.hierarchy;

import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.regex.Pattern;

/**
 * Hierarchy rules. Every method returns a readable error or null, so
 * the UI can show a clear message instead of silently rejecting an
 * operation.
 */
public final class HierarchyValidator {

    private static final Pattern ID_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

    private HierarchyValidator() {
    }

    /** Can this parent accept this child type at all? */
    public static String validateParentAccepts(ComponentModel parent, String childType) {
        if (parent == null) {
            return "No parent selected.";
        }
        ComponentDefinition parentDef = ComponentRegistry.get(parent.getType());
        if (parentDef == null || !parentDef.isContainer()) {
            return parent.getType() + " cannot contain children.";
        }
        ComponentDefinition childDef = ComponentRegistry.get(childType);
        if (childDef != null && !childDef.supportsParent(parent.getType())) {
            return childType + " cannot go inside a " + parent.getType() + ".";
        }
        if ("ScrollView".equals(parent.getType()) && !parent.getChildren().isEmpty()) {
            return "A ScrollView can have only one direct child.";
        }
        return null;
    }

    /** Can a model move under a new parent (reparenting)? */
    public static String validateReparent(ComponentModel model, ComponentModel newParent,
                                          ComponentModel root) {
        if (model == null || newParent == null) {
            return "Nothing to move.";
        }
        if (model.getId().equals(newParent.getId())) {
            return "A component cannot contain itself.";
        }
        if (ComponentHitTester.isDescendantOf(model, newParent)) {
            return "Cannot move a container inside its own child.";
        }
        ComponentModel currentParent = ComponentHitTester.findParent(root, model.getId());
        if (currentParent != null && currentParent.getId().equals(newParent.getId())) {
            return null;
        }
        String parentError = validateParentAccepts(newParent, model.getType());
        if (parentError != null) {
            return parentError;
        }
        return null;
    }

    /** Can a model move to an index inside its parent? */
    public static String validateReorder(ComponentModel model, ComponentModel parent,
                                         int newIndex) {
        if (parent == null) {
            return "No parent.";
        }
        int size = parent.getChildren().size();
        if (newIndex < 0 || newIndex >= size) {
            return "That position is out of range.";
        }
        return null;
    }

    /** ID format + uniqueness on the screen. */
    public static String validateId(ComponentModel root, String newId, String currentId) {
        if (newId == null || !ID_PATTERN.matcher(newId.trim()).matches()) {
            return "IDs must start with a letter and use only letters, digits and underscore.";
        }
        if (root != null && !newId.trim().equals(currentId)
                && root.findById(newId.trim()) != null) {
            return "That ID is already used on this screen.";
        }
        return null;
    }
}
