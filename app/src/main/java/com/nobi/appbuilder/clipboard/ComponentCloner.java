package com.nobi.appbuilder.clipboard;

import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Deep-clones components for copy/paste/duplicate. Every clone gets
 * fresh ids for itself, its children and its events, and internal
 * relationships (parent/child, event target component ids) stay valid
 * inside the clone. A clone is re-cloned on every paste, so pasting
 * the same clipboard twice can never create duplicate ids.
 */
public final class ComponentCloner {

    private ComponentCloner() {
    }

    public static ComponentModel clone(ComponentModel source) {
        return ComponentFactory.cloneTree(source);
    }

    public static List<ComponentModel> cloneList(List<ComponentModel> sources) {
        List<ComponentModel> clones = new ArrayList<>();
        for (ComponentModel source : sources) {
            clones.add(ComponentFactory.cloneTree(source));
        }
        return clones;
    }

    /** True when none of the clone ids already exist under the root. */
    public static boolean idsAreUnique(List<ComponentModel> clones,
                                       ComponentModel root) {
        Set<String> seen = new HashSet<>();
        for (ComponentModel clone : clones) {
            if (!collectIds(clone, seen, root)) {
                return false;
            }
        }
        return true;
    }

    private static boolean collectIds(ComponentModel model, Set<String> seen,
                                      ComponentModel root) {
        if (model.getId() == null || !seen.add(model.getId())) {
            return false;
        }
        if (root != null && root.findById(model.getId()) != null) {
            return false;
        }
        for (ComponentModel child : model.getChildren()) {
            if (!collectIds(child, seen, root)) {
                return false;
            }
        }
        return true;
    }
}
