package com.nobi.appbuilder.sync.mapper;

import com.nobi.appbuilder.generator.xml.XMLIdResolver;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Stable identity across JSON component id, XML android:id, visual
 * editor component and preview View. Identity is the component id —
 * never screen position. Renames update every reference; duplicate
 * XML ids are detected, never silently merged.
 */
public class ComponentSyncMapper {

    /** Component id -> android:id (the generator's sanitize rule). */
    public static String androidIdFor(String componentId) {
        return XMLIdResolver.sanitizeId(componentId);
    }

    /** "@+id/foo" or "@id/foo" -> "foo". */
    public static String componentIdFromAndroidId(String androidId) {
        if (androidId == null) {
            return null;
        }
        String value = androidId.trim();
        if (value.startsWith("@+id/")) {
            return value.substring(5);
        }
        if (value.startsWith("@id/")) {
            return value.substring(4);
        }
        return value;
    }

    public static String newComponentId(String type) {
        String base = type == null ? "view"
                : type.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        return base + "_" + UUID.randomUUID().toString()
                .substring(0, 8).replace("-", "");
    }

    /** Finds a component in a screen tree by its android:id. */
    public static ComponentModel findByAndroidId(ScreenModel screen,
                                                 String androidId) {
        if (screen == null || screen.getRootComponent() == null) {
            return null;
        }
        String componentId = componentIdFromAndroidId(androidId);
        return findById(screen.getRootComponent(), componentId);
    }

    public static ComponentModel findById(ComponentModel root,
                                          String componentId) {
        if (root == null || componentId == null) {
            return null;
        }
        if (componentId.equals(root.getId())) {
            return root;
        }
        if (root.getChildren() != null) {
            for (ComponentModel child : root.getChildren()) {
                ComponentModel found = findById(child, componentId);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** Duplicate android:ids inside one parsed tree. */
    public static List<String> findDuplicateIds(
            com.nobi.appbuilder.sync.parser.XMLParseResult.ParsedElement root) {
        List<String> seen = new ArrayList<>();
        List<String> duplicates = new ArrayList<>();
        collectIds(root, seen, duplicates);
        return duplicates;
    }

    private static void collectIds(
            com.nobi.appbuilder.sync.parser.XMLParseResult.ParsedElement element,
            List<String> seen, List<String> duplicates) {
        String androidId = element.attr("android:id");
        if (androidId != null) {
            String id = componentIdFromAndroidId(androidId);
            if (seen.contains(id)) {
                if (!duplicates.contains(id)) {
                    duplicates.add(id);
                }
            } else {
                seen.add(id);
            }
        }
        for (com.nobi.appbuilder.sync.parser.XMLParseResult.ParsedElement child
                : element.children) {
            collectIds(child, seen, duplicates);
        }
    }
}
