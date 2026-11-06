package com.nobi.appbuilder.sync.diff;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Diffs two component trees by stable component id. Detects added,
 * removed and modified components plus reordered children. Position
 * is never used as identity.
 */
public class ScreenDiffEngine {

    private final ComponentDiffEngine componentDiff =
            new ComponentDiffEngine();

    public DiffResult diff(ComponentModel oldRoot, ComponentModel newRoot) {
        DiffResult result = new DiffResult();
        if (oldRoot == null && newRoot == null) {
            return result;
        }
        if (oldRoot == null) {
            collectAdds(newRoot, result);
            return result;
        }
        if (newRoot == null) {
            collectRemoves(oldRoot, result);
            return result;
        }
        diffTree(oldRoot, newRoot, result);
        return result;
    }

    private void diffTree(ComponentModel oldNode, ComponentModel newNode,
                          DiffResult result) {
        result.addAll(componentDiff.diff(oldNode, newNode).getOperations());
        Map<String, ComponentModel> oldChildren = index(oldNode);
        Map<String, ComponentModel> newChildren = index(newNode);
        List<String> oldOrder = orderOf(oldNode);
        List<String> newOrder = orderOf(newNode);
        for (String id : newChildren.keySet()) {
            if (!oldChildren.containsKey(id)) {
                result.add(DiffOperation.add(id));
                collectAdds(newChildren.get(id), result);
            }
        }
        for (String id : oldChildren.keySet()) {
            if (!newChildren.containsKey(id)) {
                result.add(DiffOperation.remove(id));
            }
        }
        for (String id : newChildren.keySet()) {
            ComponentModel oldChild = oldChildren.get(id);
            ComponentModel newChild = newChildren.get(id);
            if (oldChild != null && newChild != null) {
                diffTree(oldChild, newChild, result);
            }
        }
        if (!oldOrder.equals(newOrder)) {
            for (String id : newOrder) {
                if (oldChildren.containsKey(id)
                        && oldOrder.indexOf(id) != newOrder.indexOf(id)) {
                    result.add(DiffOperation.move(id));
                    break;
                }
            }
        }
    }

    private Map<String, ComponentModel> index(ComponentModel node) {
        Map<String, ComponentModel> out = new HashMap<>();
        if (node.getChildren() != null) {
            for (ComponentModel child : node.getChildren()) {
                out.put(child.getId(), child);
            }
        }
        return out;
    }

    private List<String> orderOf(ComponentModel node) {
        List<String> out = new ArrayList<>();
        if (node.getChildren() != null) {
            for (ComponentModel child : node.getChildren()) {
                out.add(child.getId());
            }
        }
        return out;
    }

    private void collectAdds(ComponentModel node, DiffResult result) {
        if (node == null) {
            return;
        }
        result.add(DiffOperation.add(node.getId()));
        if (node.getChildren() != null) {
            for (ComponentModel child : node.getChildren()) {
                collectAdds(child, result);
            }
        }
    }

    private void collectRemoves(ComponentModel node, DiffResult result) {
        if (node == null) {
            return;
        }
        result.add(DiffOperation.remove(node.getId()));
        if (node.getChildren() != null) {
            for (ComponentModel child : node.getChildren()) {
                collectRemoves(child, result);
            }
        }
    }
}
