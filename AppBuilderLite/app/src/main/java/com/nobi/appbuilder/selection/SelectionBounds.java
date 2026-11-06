package com.nobi.appbuilder.selection;

import android.graphics.RectF;

import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.List;

/** The combined outline of a selection: union bounds + members. */
public class SelectionBounds {

    private final RectF bounds;
    private final List<ComponentModel> models;

    private SelectionBounds(RectF bounds, List<ComponentModel> models) {
        this.bounds = bounds;
        this.models = models;
    }

    public static SelectionBounds compute(List<ComponentModel> models,
                                          ComponentModel root) {
        List<ComponentModel> members = new ArrayList<>(models);
        RectF screenBounds = new RectF(0, 0, 360, 640);
        RectF union = null;
        for (ComponentModel model : members) {
            RectF at = ComponentHitTester.resolveBoundsFor(
                    root, model.getId(), screenBounds);
            if (at == null) {
                continue;
            }
            if (union == null) {
                union = new RectF(at);
            } else {
                union.union(at);
            }
        }
        return new SelectionBounds(
                union == null ? new RectF() : union, members);
    }

    public RectF getBounds() {
        return new RectF(bounds);
    }

    public List<ComponentModel> getModels() {
        return models;
    }

    public int getCount() {
        return models.size();
    }

    public boolean isMulti() {
        return models.size() > 1;
    }

    public boolean isEmpty() {
        return models.isEmpty();
    }

    public float getCenterX() {
        return bounds.centerX();
    }

    public float getCenterY() {
        return bounds.centerY();
    }
}
