package com.nobi.appbuilder.library;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Name/description search over library items (case-insensitive). */
public final class ComponentSearchManager {

    private ComponentSearchManager() {
    }

    public static List<ComponentLibraryItem> search(
            List<ComponentLibraryItem> items, String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(items);
        }
        String needle = query.trim().toLowerCase(Locale.US);
        List<ComponentLibraryItem> out = new ArrayList<>();
        for (ComponentLibraryItem item : items) {
            String haystack = (item.getDisplayName() + " "
                    + item.getDescription() + " " + item.getBaseType())
                    .toLowerCase(Locale.US);
            if (haystack.contains(needle)) {
                out.add(item);
            }
        }
        return out;
    }

    public static List<ComponentLibraryItem> filterByCategory(
            List<ComponentLibraryItem> items, ComponentCategory category) {
        if (category == null || category == ComponentCategory.ALL) {
            return new ArrayList<>(items);
        }
        List<ComponentLibraryItem> out = new ArrayList<>();
        for (ComponentLibraryItem item : items) {
            if (item.getCategory() == category) {
                out.add(item);
            }
        }
        return out;
    }
}
