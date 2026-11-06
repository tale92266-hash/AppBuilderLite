package com.nobi.appbuilder.generator.xml;

import java.util.HashSet;
import java.util.Set;

/**
 * Turns design ids into valid Android resource ids and screen ids
 * into Activity class names and layout file names. Ids are
 * deduplicated per generation run so a duplicated component id can
 * never produce a duplicated R.id.
 */
public class XMLIdResolver {

    private final Set<String> usedIds = new HashSet<>();

    /** Valid Android id for a component id (deduped). */
    public String resolve(String componentId) {
        String base = sanitizeId(componentId);
        String candidate = base;
        int suffix = 2;
        while (!usedIds.add(candidate)) {
            candidate = base + "_" + suffix;
            suffix++;
        }
        return candidate;
    }

    /** Sanitizes without dedup (for file/class names). */
    public static String sanitizeId(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "view";
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9') || c == '_') {
                out.append(c);
            } else {
                out.append('_');
            }
        }
        if (out.length() == 0) {
            return "view";
        }
        if (out.charAt(0) >= '0' && out.charAt(0) <= '9') {
            out.insert(0, '_');
        }
        return out.toString();
    }

    /** "home" -> "HomeActivity", "activity_home" -> "ActivityHomeActivity". */
    public static String activityClassFor(String screenId) {
        String clean = sanitizeId(screenId);
        StringBuilder out = new StringBuilder();
        boolean capitalize = true;
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (c == '_') {
                capitalize = true;
                continue;
            }
            out.append(capitalize ? Character.toUpperCase(c) : c);
            capitalize = false;
        }
        if (out.length() == 0) {
            out.append("Main");
        }
        out.append("Activity");
        return out.toString();
    }

    /** Java field name for a component id: "button_1" -> "button1". */
    public static String fieldNameFor(String componentId) {
        String clean = sanitizeId(componentId);
        StringBuilder out = new StringBuilder();
        boolean capitalize = false;
        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            if (c == '_') {
                capitalize = true;
                continue;
            }
            out.append(capitalize ? Character.toUpperCase(c) : c);
            capitalize = false;
        }
        if (out.length() == 0) {
            return "view";
        }
        return out.toString();
    }
}
