package com.nobi.appbuilder.codeeditor;

/** Language of an open file, decided by its extension. */
public enum EditorLanguage {
    JAVA,
    XML,
    GRADLE,
    TEXT,
    UNKNOWN;

    public static EditorLanguage forFileName(String name) {
        if (name == null) {
            return UNKNOWN;
        }
        String lower = name.toLowerCase();
        if (lower.endsWith(".java")) {
            return JAVA;
        }
        if (lower.endsWith(".xml")) {
            return XML;
        }
        if (lower.endsWith(".gradle") || lower.endsWith(".properties")) {
            return GRADLE;
        }
        if (lower.endsWith(".txt") || lower.endsWith(".md")) {
            return TEXT;
        }
        return UNKNOWN;
    }

    public String getDisplayName() {
        switch (this) {
            case JAVA:
                return "Java";
            case XML:
                return "XML";
            case GRADLE:
                return "Gradle";
            case TEXT:
                return "Text";
            default:
                return "Unknown";
        }
    }
}
