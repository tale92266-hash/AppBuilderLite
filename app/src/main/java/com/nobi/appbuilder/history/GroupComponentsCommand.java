package com.nobi.appbuilder.history;

import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.hierarchy.HierarchyValidator;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Groups sibling components into a new container (FrameLayout,
 * LinearLayout or ConstraintLayout). Visual positions are preserved:
 * the group sits at the siblings' top-left corner and each child
 * keeps its offset inside it. When positions cannot be preserved
 * (mixed parents, a ScrollView that would gain a second child),
 * the command refuses with a clear reason instead of moving things
 * silently. Ungrouping is the nested UngroupCommand.
 */
public class GroupComponentsCommand implements EditorCommand {

    public static final String[] GROUP_CONTAINER_TYPES = {
            "FrameLayout", "LinearLayout", "ConstraintLayout"
    };

    private final ComponentModel root;
    private final List<ComponentModel> models = new ArrayList<>();
    private final List<Integer> originalIndices = new ArrayList<>();
    private final List<int[]> originalPositions = new ArrayList<>();
    private final ComponentModel container;
    private ComponentModel parent;
    private String validationError;

    public GroupComponentsCommand(ComponentModel root, List<ComponentModel> models,
                                  String containerType) {
        this.root = root;
        for (ComponentModel model : models) {
            this.models.add(model);
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxRight = Integer.MIN_VALUE;
        int maxBottom = Integer.MIN_VALUE;
        for (ComponentModel model : models) {
            minX = Math.min(minX, model.getX());
            minY = Math.min(minY, model.getY());
            maxRight = Math.max(maxRight, model.getX() + model.getWidth());
            maxBottom = Math.max(maxBottom, model.getY() + model.getHeight());
        }
        this.container = ComponentFactory.create(containerType,
                minX == Integer.MAX_VALUE ? 0 : minX,
                minY == Integer.MAX_VALUE ? 0 : minY);
        if (maxRight > minX) {
            container.setWidth(maxRight - minX);
            container.setHeight(maxBottom - minY);
            container.getLayout().setWidth(
                    com.nobi.appbuilder.model.LayoutModel.dimensionToString(
                            container.getWidth()));
            container.getLayout().setHeight(
                    com.nobi.appbuilder.model.LayoutModel.dimensionToString(
                            container.getHeight()));
        }
    }

    @Override
    public boolean canExecute() {
        validationError = validate();
        return validationError == null;
    }

    public String getValidationError() {
        return validationError;
    }

    private String validate() {
        if (models.size() < 2) {
            return "Select at least two components to group.";
        }
        parent = ComponentHitTester.findParent(root, models.get(0).getId());
        if (parent == null) {
            return "These components cannot be grouped.";
        }
        for (ComponentModel model : models) {
            if (model.isLocked()) {
                return model.getType() + " is locked and cannot be grouped.";
            }
            ComponentModel modelParent =
                    ComponentHitTester.findParent(root, model.getId());
            if (modelParent == null || modelParent != parent) {
                return "Only components with the same parent can be grouped.";
            }
        }
        String error = HierarchyValidator.validateParentAccepts(
                parent, container.getType());
        if (error != null) {
            return error;
        }
        if ("ScrollView".equals(parent.getType())
                && parent.getChildren().size() != models.size()) {
            return "A ScrollView can have only one direct child, so these "
                    + "components cannot be grouped inside it.";
        }
        ComponentModel probe = ComponentFactory.create(container.getType(), 0, 0);
        for (ComponentModel model : models) {
            String childError = HierarchyValidator.validateParentAccepts(
                    probe, model.getType());
            if (childError != null) {
                return childError;
            }
        }
        return null;
    }

    @Override
    public void execute() {
        originalIndices.clear();
        originalPositions.clear();
        for (ComponentModel model : models) {
            originalIndices.add(parent.getChildren().indexOf(model));
            originalPositions.add(new int[]{model.getX(), model.getY()});
        }
        int at = originalIndices.get(0);
        for (int index : originalIndices) {
            at = Math.min(at, index);
        }
        parent.getChildren().add(at, container);
        for (int i = 0; i < models.size(); i++) {
            ComponentModel model = models.get(i);
            parent.getChildren().remove(model);
            int[] original = originalPositions.get(i);
            model.setX(original[0] - container.getX());
            model.setY(original[1] - container.getY());
            container.addChild(model);
        }
    }

    @Override
    public void undo() {
        container.getChildren().clear();
        parent.getChildren().remove(container);
        List<Integer> indices = new ArrayList<>(originalIndices);
        List<ComponentModel> ordered = new ArrayList<>(models);
        for (int i = 0; i < ordered.size() - 1; i++) {
            for (int j = i + 1; j < ordered.size(); j++) {
                if (indices.get(j) < indices.get(i)) {
                    int tmpIndex = indices.get(i);
                    indices.set(i, indices.get(j));
                    indices.set(j, tmpIndex);
                    ComponentModel tmp = ordered.get(i);
                    ordered.set(i, ordered.get(j));
                    ordered.set(j, tmp);
                }
            }
        }
        for (int i = 0; i < ordered.size(); i++) {
            ComponentModel model = ordered.get(i);
            int[] original = originalPositions.get(models.indexOf(model));
            model.setX(original[0]);
            model.setY(original[1]);
            int at = Math.max(0, Math.min(indices.get(i),
                    parent.getChildren().size()));
            parent.getChildren().add(at, model);
        }
    }

    @Override
    public String getDescription() {
        return "Group " + models.size() + " components";
    }

    public ComponentModel getContainer() {
        return container;
    }

    /** Ungroups a container: children return to its parent, container goes. */
    public static class UngroupCommand implements EditorCommand {

        private final ComponentModel root;
        private final ComponentModel container;
        private ComponentModel parent;
        private int containerIndex = -1;
        private final List<ComponentModel> children = new ArrayList<>();
        private final List<int[]> relativePositions = new ArrayList<>();

        public UngroupCommand(ComponentModel root, ComponentModel container) {
            this.root = root;
            this.container = container;
        }

        @Override
        public boolean canExecute() {
            parent = ComponentHitTester.findParent(root, container.getId());
            if (parent == null) {
                return false;
            }
            containerIndex = parent.getChildren().indexOf(container);
            return containerIndex >= 0 && !container.getChildren().isEmpty()
                    && !container.isLocked();
        }

        @Override
        public void execute() {
            children.clear();
            relativePositions.clear();
            children.addAll(container.getChildren());
            for (ComponentModel child : children) {
                relativePositions.add(new int[]{child.getX(), child.getY()});
            }
            container.getChildren().clear();
            parent.getChildren().remove(container);
            for (int i = 0; i < children.size(); i++) {
                ComponentModel child = children.get(i);
                int[] relative = relativePositions.get(i);
                child.setX(container.getX() + relative[0]);
                child.setY(container.getY() + relative[1]);
                int at = Math.min(containerIndex + i, parent.getChildren().size());
                parent.getChildren().add(at, child);
            }
        }

        @Override
        public void undo() {
            for (ComponentModel child : children) {
                parent.getChildren().remove(child);
            }
            container.getChildren().clear();
            for (int i = 0; i < children.size(); i++) {
                ComponentModel child = children.get(i);
                int[] relative = relativePositions.get(i);
                child.setX(relative[0]);
                child.setY(relative[1]);
                container.addChild(child);
            }
            int at = Math.max(0, Math.min(containerIndex,
                    parent.getChildren().size()));
            parent.getChildren().add(at, container);
        }

        @Override
        public String getDescription() {
            return "Ungroup " + container.getType();
        }
    }
}
