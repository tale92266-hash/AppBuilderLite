package com.nobi.appbuilder.screens;

import com.nobi.appbuilder.model.ScreenModel;

import java.util.List;

/** Validation for screen names, types and destructive operations. */
public final class ScreenValidator {

    private ScreenValidator() {
    }

    /** Returns an error string, or null when the name is usable. */
    public static String validateName(String name, List<ScreenModel> existing,
                                      String excludeScreenId) {
        if (name == null || name.trim().isEmpty()) {
            return "Screen name cannot be empty.";
        }
        if (name.trim().length() > 60) {
            return "Screen name is too long (max 60 characters).";
        }
        for (ScreenModel screen : existing) {
            if (screen.getId() != null && screen.getId().equals(excludeScreenId)) {
                continue;
            }
            if (screen.getName() != null
                    && screen.getName().equalsIgnoreCase(name.trim())) {
                return "A screen named \"" + name.trim() + "\" already exists.";
            }
        }
        return null;
    }

    public static String validateType(String type) {
        return ScreenModel.isValidScreenType(type) ? null : "Unknown screen type.";
    }

    /** The last remaining screen must never be deleted. */
    public static String validateDelete(int screenCount) {
        return screenCount <= 1
                ? "A project needs at least one screen."
                : null;
    }
}
