package com.nobi.appbuilder.history;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Moves one or more components. Built once when a drag completes, so a
 * whole drag is a single history entry however far it travelled.
 */
public class MoveComponentCommand implements EditorCommand {

    private final List<ComponentModel> models = new ArrayList<>();
    private final Map<String, float[]> startPositions = new LinkedHashMap<>();
    private final Map<String, float[]> endPositions = new LinkedHashMap<>();

    public MoveComponentCommand(List<ComponentModel> models,
                                Map<String, float[]> startPositions,
                                Map<String, float[]> endPositions) {
        for (ComponentModel model : models) {
            this.models.add(model);
            float[] start = startPositions.get(model.getId());
            float[] end = endPositions.get(model.getId());
            if (start != null) {
                startPositions.put(model.getId(), new float[]{start[0], start[1]});
            }
            if (end != null) {
                endPositions.put(model.getId(), new float[]{end[0], end[1]});
            }
        }
    }

    @Override
    public boolean canExecute() {
        if (models.isEmpty()) {
            return false;
        }
        for (ComponentModel model : models) {
            if (model.isLocked()) {
                return false;
            }
            float[] start = startPositions.get(model.getId());
            float[] end = endPositions.get(model.getId());
            if (start == null || end == null) {
                return false;
            }
            if (start[0] != end[0] || start[1] != end[1]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void execute() {
        apply(endPositions);
    }

    @Override
    public void undo() {
        apply(startPositions);
    }

    private void apply(Map<String, float[]> positions) {
        for (ComponentModel model : models) {
            float[] at = positions.get(model.getId());
            if (at != null) {
                model.setX(Math.round(at[0]));
                model.setY(Math.round(at[1]));
            }
        }
    }

    @Override
    public String getDescription() {
        return models.size() == 1
                ? "Move " + models.get(0).getType()
                : "Move " + models.size() + " components";
    }
}
