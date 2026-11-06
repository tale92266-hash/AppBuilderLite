package com.nobi.appbuilder.sync.diff;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.HashMap;
import java.util.Map;

/** Diffs two versions of one component (properties + layout). */
public class ComponentDiffEngine {

    private final PropertyDiffEngine propertyDiff = new PropertyDiffEngine();

    public DiffResult diff(ComponentModel oldModel, ComponentModel newModel) {
        DiffResult result = new DiffResult();
        if (oldModel == null || newModel == null) {
            return result;
        }
        String id = oldModel.getId();
        if (!oldModel.getType().equals(newModel.getType())) {
            result.add(DiffOperation.update(id, "__type__",
                    oldModel.getType(), newModel.getType()));
        }
        result.addAll(propertyDiff.diff(id, oldModel.getProperties(),
                newModel.getProperties()).getOperations());
        Map<String, String> oldLayout = layoutMap(oldModel);
        Map<String, String> newLayout = layoutMap(newModel);
        result.addAll(propertyDiff.diff(id, oldLayout, newLayout)
                .getOperations());
        return result;
    }

    private Map<String, String> layoutMap(ComponentModel model) {
        Map<String, String> out = new HashMap<>();
        if (model.getLayout() == null) {
            return out;
        }
        com.nobi.appbuilder.model.LayoutModel layout = model.getLayout();
        out.put("width", layout.getWidth());
        out.put("height", layout.getHeight());
        out.put("marginLeft", String.valueOf(layout.getMarginLeft()));
        out.put("marginTop", String.valueOf(layout.getMarginTop()));
        out.put("marginRight", String.valueOf(layout.getMarginRight()));
        out.put("marginBottom", String.valueOf(layout.getMarginBottom()));
        out.put("paddingLeft", String.valueOf(layout.getPaddingLeft()));
        out.put("paddingTop", String.valueOf(layout.getPaddingTop()));
        out.put("paddingRight", String.valueOf(layout.getPaddingRight()));
        out.put("paddingBottom", String.valueOf(layout.getPaddingBottom()));
        out.put("gravity", layout.getGravity());
        out.put("layoutGravity", layout.getLayoutGravity());
        out.put("weight", String.valueOf(layout.getWeight()));
        return out;
    }
}
