package com.nobi.appbuilder.editor;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Everything about the editing session that is NOT saved in the JSON
 * document: the open screen, the selection, the active tool, the
 * clipboard, which hierarchy nodes are expanded, grid/snap flags.
 * Selection state deliberately lives here, never in the document.
 */
public class CanvasState {

    public static final float DEFAULT_DESIGN_WIDTH_DP = 360f;
    public static final float DEFAULT_DESIGN_HEIGHT_DP = 640f;

    private ScreenModel screen;
    private ComponentModel root;
    private final Set<String> selectedIds = new LinkedHashSet<>();
    private GestureController.Tool currentTool;
    private ComponentModel clipboard;
    private final Set<String> expandedIds = new LinkedHashSet<>();
    private float designWidthDp;
    private float designHeightDp;
    private boolean gridVisible;
    private boolean snapEnabled;

    public CanvasState() {
        designWidthDp = DEFAULT_DESIGN_WIDTH_DP;
        designHeightDp = DEFAULT_DESIGN_HEIGHT_DP;
        gridVisible = true;
        snapEnabled = true;
    }

    public ScreenModel getScreen() {
        return screen;
    }

    public void setScreen(ScreenModel screen) {
        this.screen = screen;
    }

    public ComponentModel getRoot() {
        return root;
    }

    public void setRoot(ComponentModel root) {
        this.root = root;
        selectedIds.clear();
        expandedIds.clear();
        if (root != null) {
            expandedIds.add(root.getId());
        }
    }

    public float getDesignWidthDp() {
        return designWidthDp;
    }

    public float getDesignHeightDp() {
        return designHeightDp;
    }

    public boolean isGridVisible() {
        return gridVisible;
    }

    public void setGridVisible(boolean gridVisible) {
        this.gridVisible = gridVisible;
    }

    public boolean isSnapEnabled() {
        return snapEnabled;
    }

    public void setSnapEnabled(boolean snapEnabled) {
        this.snapEnabled = snapEnabled;
    }

    // ------------------------------------------------------------------
    // Selection
    // ------------------------------------------------------------------

    public void select(String componentId) {
        selectedIds.clear();
        if (componentId != null) {
            selectedIds.add(componentId);
        }
    }

    public void addToSelection(String componentId) {
        if (componentId != null) {
            selectedIds.add(componentId);
        }
    }

    public void toggleSelection(String componentId) {
        if (componentId == null) {
            return;
        }
        if (!selectedIds.remove(componentId)) {
            selectedIds.add(componentId);
        }
    }

    public void clearSelection() {
        selectedIds.clear();
    }

    public boolean isSelected(String componentId) {
        return componentId != null && selectedIds.contains(componentId);
    }

    public Set<String> getSelectedIds() {
        return new LinkedHashSet<>(selectedIds);
    }

    public List<ComponentModel> getSelectedModels() {
        List<ComponentModel> models = new ArrayList<>();
        if (root == null) {
            return models;
        }
        for (String id : selectedIds) {
            ComponentModel model = root.findById(id);
            if (model != null) {
                models.add(model);
            }
        }
        return models;
    }

    public ComponentModel getSingleSelection() {
        List<ComponentModel> models = getSelectedModels();
        return models.size() == 1 ? models.get(0) : null;
    }

    public boolean hasSelection() {
        return !selectedIds.isEmpty();
    }

    // ------------------------------------------------------------------
    // Tool, clipboard, hierarchy expansion
    // ------------------------------------------------------------------

    public GestureController.Tool getCurrentTool() {
        return currentTool;
    }

    public void setCurrentTool(GestureController.Tool tool) {
        currentTool = tool;
    }

    public ComponentModel getClipboard() {
        return clipboard;
    }

    public void setClipboard(ComponentModel clipboard) {
        this.clipboard = clipboard;
    }

    public boolean hasClipboard() {
        return clipboard != null;
    }

    public boolean isExpanded(String componentId) {
        return componentId != null && expandedIds.contains(componentId);
    }

    public void toggleExpanded(String componentId) {
        if (componentId == null) {
            return;
        }
        if (!expandedIds.remove(componentId)) {
            expandedIds.add(componentId);
        }
    }

    public void setExpanded(String componentId, boolean expanded) {
        if (componentId == null) {
            return;
        }
        if (expanded) {
            expandedIds.add(componentId);
        } else {
            expandedIds.remove(componentId);
        }
    }
}
