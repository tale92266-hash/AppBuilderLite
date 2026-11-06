package com.nobi.appbuilder.assets.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Detects resource-name collisions and resolves them with suffixes. */
public final class ResourceConflictManager {

    private ResourceConflictManager() {
    }

    public static List<String> findConflicts(List<String> names) {
        List<String> conflicts = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String name : names) {
            if (name == null) {
                continue;
            }
            if (!seen.add(name) && !conflicts.contains(name)) {
                conflicts.add(name);
            }
        }
        return conflicts;
    }

    public static String resolve(String base, Set<String> taken) {
        return ResourceNamingManager.uniqueName(base, taken);
    }

    public static boolean hasConflict(String name, Set<String> taken) {
        return taken != null && taken.contains(name);
    }
}
