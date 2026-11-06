package com.nobi.appbuilder.sync.diff;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** Diffs two flat property maps. */
public class PropertyDiffEngine {

    public DiffResult diff(String componentId,
                           Map<String, String> oldProps,
                           Map<String, String> newProps) {
        DiffResult result = new DiffResult();
        Set<String> keys = new HashSet<>();
        if (oldProps != null) {
            keys.addAll(oldProps.keySet());
        }
        if (newProps != null) {
            keys.addAll(newProps.keySet());
        }
        for (String key : keys) {
            String oldValue = oldProps == null ? null : oldProps.get(key);
            String newValue = newProps == null ? null : newProps.get(key);
            if (oldValue == null && newValue == null) {
                continue;
            }
            if (oldValue != null && oldValue.equals(newValue)) {
                continue;
            }
            result.add(DiffOperation.update(componentId, key,
                    oldValue, newValue));
        }
        return result;
    }
}
