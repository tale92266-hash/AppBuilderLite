package com.nobi.appbuilder.preview;

import android.content.Context;

import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.preview.events.PreviewActionExecutor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks a screen before it renders. Every problem is reported as
 * "componentId: message" so the user knows exactly what to fix.
 * Problems are recoverable: the renderer still draws the screen and
 * swaps each bad component for an error placeholder instead of
 * crashing the whole preview.
 */
public class PreviewValidator {

    private final PreviewActionExecutor executor = new PreviewActionExecutor();

    /**
     * Validates one screen against its document (for navigation
     * targets), the project's assets (for images) and the component
     * registry. Returns an empty list when the screen is clean.
     */
    public List<String> validate(IRDocument document, ScreenModel screen,
                                 AssetRepository assetRepository,
                                 String projectId, Context context) {
        List<String> errors = new ArrayList<>();
        if (screen == null) {
            errors.add("screen: no screen to preview");
            return errors;
        }
        ComponentModel root = screen.getRootComponent();
        if (root == null) {
            errors.add(screen.getId() + ": screen has no root component");
            return errors;
        }
        Set<String> seenIds = new HashSet<>();
        validateComponent(document, root, null, seenIds, errors,
                assetRepository, projectId, context);
        return errors;
    }

    private void validateComponent(IRDocument document, ComponentModel model,
                                   ComponentModel parent, Set<String> seenIds,
                                   List<String> errors,
                                   AssetRepository assetRepository,
                                   String projectId, Context context) {
        String id = model.getId();
        if (id == null || id.isEmpty()) {
            errors.add("?: component without an id (" + model.getType() + ")");
        } else if (!seenIds.add(id)) {
            errors.add(id + ": duplicate component id");
        }
        String type = model.getType();
        if (type == null || !ComponentRegistry.contains(type)) {
            errors.add(id + ": unknown component type '" + type + "'");
            return;
        }
        if (model.getWidth() < -2 || model.getHeight() < -2) {
            errors.add(id + ": invalid dimensions");
        }
        if ("ScrollView".equals(type) && model.getChildren().size() > 1) {
            errors.add(id + ": ScrollView must have exactly one direct child");
        }
        if (parent != null && "ConstraintLayout".equals(parent.getType())) {
            validateConstraints(model, parent, errors);
        }
        if ("ImageView".equals(type)) {
            validateImage(model, assetRepository, projectId, context, errors);
        }
        for (EventModel event : model.getEvents()) {
            validateEvent(document, model, event, errors);
        }
        for (ComponentModel child : model.getChildren()) {
            validateComponent(document, child, model, seenIds, errors,
                    assetRepository, projectId, context);
        }
    }

    private void validateConstraints(ComponentModel model, ComponentModel parent,
                                     List<String> errors) {
        for (String key : new String[]{"constraintTop", "constraintBottom",
                "constraintStart", "constraintEnd"}) {
            String target = model.getProperty(key);
            if (target == null || "none".equals(target)
                    || "parent".equals(target)) {
                continue;
            }
            // A target must be a sibling id; the renderer resolves it
            // through the context, so an unknown id is a real error.
            boolean found = false;
            for (ComponentModel sibling : parent.getChildren()) {
                if (target.equals(sibling.getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                errors.add(model.getId() + ": " + key
                        + " target '" + target + "' not found among siblings");
            }
        }
    }

    private void validateImage(ComponentModel model,
                               AssetRepository assetRepository,
                               String projectId, Context context,
                               List<String> errors) {
        String src = model.getProperty("src");
        if (src == null || src.isEmpty()) {
            return;
        }
        if (context != null && context.getResources().getIdentifier(
                src, "drawable", context.getPackageName()) != 0) {
            return;
        }
        if (assetRepository != null && projectId != null) {
            for (AssetRepository.Asset asset : assetRepository.getByProject(projectId)) {
                if (src.equals(asset.id) || src.equals(asset.name)) {
                    return;
                }
            }
        }
        if (src.startsWith("/") && new java.io.File(src).exists()) {
            return;
        }
        errors.add(model.getId() + ": image '" + src + "' not found");
    }

    private void validateEvent(IRDocument document, ComponentModel model,
                               EventModel event, List<String> errors) {
        if (event == null) {
            return;
        }
        if (!executor.isSupported(event.getAction())) {
            errors.add(model.getId() + ": unsupported event action '"
                    + event.getAction() + "'");
            return;
        }
        if (PreviewActionExecutor.ACTION_NAVIGATE.equals(event.getAction())) {
            String target = event.getTargetScreenId();
            if (target == null || target.isEmpty()) {
                errors.add(model.getId()
                        + ": navigate event has no target screen");
            } else if (document != null && document.getScreen(target) == null) {
                errors.add(model.getId() + ": navigate target screen '"
                        + target + "' does not exist");
            }
        }
    }
}
