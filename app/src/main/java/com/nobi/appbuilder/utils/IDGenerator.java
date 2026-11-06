package com.nobi.appbuilder.utils;

import java.util.UUID;

/**
 * Generates unique ids for projects, screens, components and assets.
 * Ids are short, readable and unique across projects.
 */
public final class IDGenerator {

    private IDGenerator() {
    }

    public static String newId(String prefix) {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return prefix + "_" + random;
    }

    /** Generic id when the caller has no domain-specific prefix. */
    public static String newId() {
        return newId("id");
    }

    public static String newProjectId() {
        return newId("proj");
    }

    public static String newScreenId() {
        return newId("scr");
    }

    public static String newComponentId() {
        return newId("comp");
    }

    public static String newAssetId() {
        return newId("asset");
    }

    public static String newEventId() {
        return newId("evt");
    }
}
