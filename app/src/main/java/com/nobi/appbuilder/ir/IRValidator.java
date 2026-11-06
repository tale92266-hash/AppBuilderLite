package com.nobi.appbuilder.ir;

import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.model.LayoutModel;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates documents, projects, screens and component trees. Every
 * method returns the list of problems found (empty = valid) instead of
 * throwing, so callers can show all of them at once. Unknown property
 * keys are allowed — validation never rejects data it does not understand.
 */
public final class IRValidator {

    private IRValidator() {
    }

    public static List<String> validateDocument(IRDocument document) {
        List<String> errors = new ArrayList<>();
        if (document == null) {
            errors.add("Document is null.");
            return errors;
        }
        int version = document.getSchemaVersion();
        if (version < IRConstants.MIN_SUPPORTED_SCHEMA_VERSION
                || version > IRConstants.CURRENT_SCHEMA_VERSION) {
            errors.add("Unsupported schema version: " + version + ".");
        }
        if (document.getProject() == null) {
            errors.add("Document has no project.");
            return errors;
        }
        errors.addAll(validateProject(document.getProject()));
        List<ScreenModel> screens = document.getScreens();
        if (screens.size() > IRConstants.MAX_SCREENS_PER_PROJECT) {
            errors.add("Project has too many screens: " + screens.size() + ".");
        }
        Set<String> screenIds = new HashSet<>();
        Set<String> screenNames = new HashSet<>();
        int launcherCount = 0;
        for (ScreenModel screen : screens) {
            if (screen == null) {
                errors.add("Project contains an empty screen.");
                continue;
            }
            if (screen.getId() == null || screen.getId().trim().isEmpty()) {
                errors.add("A screen has no id.");
            } else if (!screenIds.add(screen.getId())) {
                errors.add("Duplicate screen id: " + screen.getId() + ".");
            }
            if (screen.getName() == null || screen.getName().trim().isEmpty()) {
                errors.add("Screen " + screen.getId() + " has no name.");
            } else if (!screenNames.add(screen.getName())) {
                errors.add("Duplicate screen name: " + screen.getName() + ".");
            }
            if (screen.isLauncher()) {
                launcherCount++;
            }
            errors.addAll(validateScreen(screen));
        }
        if (!screens.isEmpty() && launcherCount == 0) {
            errors.add("Project has no launcher screen.");
        } else if (launcherCount > 1) {
            errors.add("Project has more than one launcher screen.");
        }
        return errors;
    }

    public static List<String> validateProject(ProjectModel project) {
        List<String> errors = new ArrayList<>();
        if (project == null) {
            errors.add("Project is null.");
            return errors;
        }
        if (!ProjectModel.isValidAppName(project.getAppName())) {
            errors.add("Project has no valid application name.");
        }
        if (!ProjectModel.isValidPackageName(project.getPackageName())) {
            errors.add("Invalid package name: " + project.getPackageName() + ".");
        }
        if (project.getMinSdk() < IRConstants.MIN_SDK
                || project.getMinSdk() > IRConstants.MAX_SDK) {
            errors.add("Minimum SDK out of range: " + project.getMinSdk() + ".");
        }
        if (project.getSchemaVersion() < 1) {
            errors.add("Project schema version is missing.");
        }
        return errors;
    }

    public static List<String> validateScreen(ScreenModel screen) {
        List<String> errors = new ArrayList<>();
        if (screen == null) {
            errors.add("Screen is null.");
            return errors;
        }
        if (screen.getName() == null || screen.getName().trim().isEmpty()) {
            errors.add("Screen has no name.");
        }
        String designJson = screen.getDesignJson();
        if (designJson != null && !designJson.trim().isEmpty()) {
            ComponentModel root = screen.getRootComponent();
            if (root == null) {
                errors.add("Screen '" + screen.getName()
                        + "' has a design that could not be read.");
            } else {
                IRDeserializer.normalizeComponent(root);
                errors.addAll(validateComponentTree(root));
            }
        }
        return errors;
    }

    /** Validates a whole component tree, including duplicate ids. */
    public static List<String> validateComponentTree(ComponentModel root) {
        List<String> errors = new ArrayList<>();
        if (root == null) {
            errors.add("Component tree is empty.");
            return errors;
        }
        validateComponent(root, new HashSet<String>(), errors, root.getType());
        return errors;
    }

    private static void validateComponent(ComponentModel component, Set<String> seenIds,
                                          List<String> errors, String path) {
        if (component == null) {
            errors.add("Empty component at " + path + ".");
            return;
        }
        String id = component.getId();
        if (id == null || id.trim().isEmpty()) {
            errors.add("A component at " + path + " has no id.");
        } else if (!seenIds.add(id)) {
            errors.add("Duplicate component id: " + id + ".");
        }
        String type = component.getType();
        if (type == null || type.trim().isEmpty()) {
            errors.add("Component " + id + " has no type.");
        } else if (!ComponentRegistry.contains(type)) {
            errors.add("Unsupported component type: " + type + " (id " + id + ").");
        }
        int width = component.getWidth();
        int height = component.getHeight();
        if (width != ComponentModel.MATCH_PARENT
                && width != ComponentModel.WRAP_CONTENT
                && (width < 0 || width > IRConstants.MAX_DIMENSION_DP)) {
            errors.add("Invalid width for component " + id + ": " + width + ".");
        }
        if (height != ComponentModel.MATCH_PARENT
                && height != ComponentModel.WRAP_CONTENT
                && (height < 0 || height > IRConstants.MAX_DIMENSION_DP)) {
            errors.add("Invalid height for component " + id + ": " + height + ".");
        }
        if (component.getX() < 0 || component.getX() > IRConstants.MAX_DIMENSION_DP
                || component.getY() < 0 || component.getY() > IRConstants.MAX_DIMENSION_DP) {
            errors.add("Invalid position for component " + id + ".");
        }
        LayoutModel layout = component.getLayout();
        if (layout != null) {
            if (!isValidDimensionString(layout.getWidth())
                    || !isValidDimensionString(layout.getHeight())) {
                errors.add("Invalid layout size for component " + id + ".");
            }
        }
        String textSize = component.getProperty("textSize");
        if (textSize != null) {
            try {
                float size = Float.parseFloat(textSize.trim());
                if (size <= 0 || size > 200) {
                    errors.add("Invalid textSize for component " + id + ": " + textSize + ".");
                }
            } catch (NumberFormatException e) {
                errors.add("Invalid textSize for component " + id + ": " + textSize + ".");
            }
        }
        for (String colorKey : new String[]{"textColor", "backgroundColor"}) {
            String color = component.getProperty(colorKey);
            if (color != null && !isValidColor(color)) {
                errors.add("Invalid " + colorKey + " for component " + id + ": " + color + ".");
            }
        }
        String text = component.getProperty("text");
        if (text != null && text.length() > IRConstants.MAX_TEXT_LENGTH) {
            errors.add("Text too long for component " + id + ".");
        }
        if (component.getEvents() != null) {
            for (EventModel event : component.getEvents()) {
                if (event == null) {
                    continue;
                }
                String action = event.getAction();
                if (action != null
                        && !IRConstants.ACTION_NONE.equals(action)
                        && !IRConstants.ACTION_TOAST.equals(action)
                        && !IRConstants.ACTION_NAVIGATE.equals(action)) {
                    errors.add("Invalid event action for component " + id + ": " + action + ".");
                }
            }
        }
        if (component.getChildren() != null) {
            if (component.getChildren().size() > IRConstants.MAX_COMPONENTS_PER_SCREEN) {
                errors.add("Too many children in component " + id + ".");
            }
            for (ComponentModel child : component.getChildren()) {
                validateComponent(child, seenIds, errors, path + "/" + id);
            }
        }
    }

    private static boolean isValidDimensionString(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim().toLowerCase();
        if (LayoutModel.MATCH_PARENT.equals(v) || LayoutModel.WRAP_CONTENT.equals(v)) {
            return true;
        }
        return LayoutModel.parseDimension(v, -999) != -999;
    }

    private static boolean isValidColor(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim();
        return v.matches("#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})");
    }

    public static boolean isValid(IRDocument document) {
        return validateDocument(document).isEmpty();
    }
}
