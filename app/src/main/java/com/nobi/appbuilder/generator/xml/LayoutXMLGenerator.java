package com.nobi.appbuilder.generator.xml;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.mapping.ComponentMappingRegistry;
import com.nobi.appbuilder.generator.mapping.PropertyMappingRegistry;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.LayoutModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recursively converts a ComponentModel tree into XML elements.
 * Layout parameters are generated per parent type (Linear weight /
 * gravity, Frame gravity, ConstraintLayout constraints + bias,
 * ScrollView fill viewport). A layout that cannot be represented
 * faithfully produces a warning instead of a silently different
 * layout — e.g. a ScrollView with extra children, or a constraint
 * pointing at a component that does not exist.
 */
public class LayoutXMLGenerator {

    private final GenerationContext context;
    private final XMLAttributeMapper attributeMapper;
    private int componentCount;

    public LayoutXMLGenerator(GenerationContext context) {
        this.context = context;
        this.attributeMapper = new XMLAttributeMapper(context);
    }

    public int getComponentCount() {
        return componentCount;
    }

    /** Writes one component (and its subtree) into the writer. */
    public void writeComponent(XMLDocumentWriter writer, ComponentModel model,
                               String parentType) {
        String tag = ComponentMappingRegistry.xmlTagFor(model.getType());
        if (tag == null) {
            context.addWarning("xml", null, model.getId(),
                    "unsupported component type '" + model.getType()
                            + "' — skipped from layout");
            return;
        }
        componentCount++;
        Map<String, String> attrs = new LinkedHashMap<>();
        String generatedId = context.getIdResolver().resolve(model.getId());
        context.recordResolvedId(model.getId(), generatedId);
        attrs.put("android:id", "@+id/" + generatedId);
        attrs.put("android:layout_width", dimension(model, true));
        attrs.put("android:layout_height", dimension(model, false));
        addLayoutParams(attrs, model, parentType);
        addComponentAttributes(writer, model, attrs);

        List<ComponentModel> children = model.getChildren();
        boolean isContainer = "LinearLayout".equals(model.getType())
                || "FrameLayout".equals(model.getType())
                || "ConstraintLayout".equals(model.getType())
                || "ScrollView".equals(model.getType())
                || "MaterialCardView".equals(model.getType());
        if (!isContainer || children.isEmpty()) {
            writer.emptyElement(tag, attrs);
            return;
        }
        if ("ScrollView".equals(model.getType()) && children.size() > 1) {
            context.addWarning("xml", null, model.getId(),
                    "ScrollView has " + children.size()
                            + " children; only the first is generated");
            children = children.subList(0, 1);
        }
        writer.startElement(tag, attrs);
        for (ComponentModel child : children) {
            writeComponent(writer, child, model.getType());
        }
        writer.endElement();
    }

    private String dimension(ComponentModel model, boolean width) {
        LayoutModel layout = model.getLayout();
        String fromLayout = layout == null ? null
                : (width ? layout.getWidth() : layout.getHeight());
        if (fromLayout != null && !fromLayout.isEmpty()) {
            String value = fromLayout.trim();
            if (LayoutModel.MATCH_PARENT.equals(value)
                    || LayoutModel.WRAP_CONTENT.equals(value)) {
                return value;
            }
            int dp = LayoutModel.parseDimension(value, Integer.MIN_VALUE);
            if (dp != Integer.MIN_VALUE) {
                return dp + "dp";
            }
        }
        int modelValue = width ? model.getWidth() : model.getHeight();
        return LayoutModel.dimensionToString(modelValue);
    }

    private void addLayoutParams(Map<String, String> attrs, ComponentModel model,
                                 String parentType) {
        LayoutModel layout = model.getLayout();
        if (layout != null) {
            putMargin(attrs, "android:layout_marginLeft", layout.getMarginLeft());
            putMargin(attrs, "android:layout_marginTop", layout.getMarginTop());
            putMargin(attrs, "android:layout_marginRight", layout.getMarginRight());
            putMargin(attrs, "android:layout_marginBottom", layout.getMarginBottom());
        }
        if ("LinearLayout".equals(parentType)) {
            if (layout != null && layout.getWeight() > 0) {
                attrs.put("android:layout_weight",
                        PropertyMappingRegistry.formatNumber(layout.getWeight()));
            }
            putGravity(attrs, layout, "android:layout_gravity");
        } else if ("FrameLayout".equals(parentType)) {
            putGravity(attrs, layout, "android:layout_gravity");
        } else if ("ConstraintLayout".equals(parentType)) {
            addConstraints(attrs, model);
        }
        if ("LinearLayout".equals(model.getType())) {
            String orientation = model.getProperty("orientation");
            attrs.put("android:orientation",
                    "horizontal".equalsIgnoreCase(orientation)
                            ? "horizontal" : "vertical");
            putGravity(attrs, layout, "android:gravity");
        }
        if ("ScrollView".equals(model.getType())) {
            attrs.put("android:fillViewport", "true");
        }
    }

    private void putGravity(Map<String, String> attrs, LayoutModel layout,
                            String key) {
        if (layout == null || layout.getLayoutGravity() == null) {
            if ("android:gravity".equals(key) && layout != null
                    && layout.getGravity() != null) {
                attrs.put(key, mapGravity(layout.getGravity()));
            }
            return;
        }
        attrs.put(key, mapGravity(layout.getLayoutGravity()));
    }

    private String mapGravity(String gravity) {
        if (gravity == null) {
            return null;
        }
        switch (gravity.trim().toLowerCase()) {
            case "left":
                return "start";
            case "right":
                return "end";
            case "center":
                return "center";
            case "center_horizontal":
                return "center_horizontal";
            case "center_vertical":
                return "center_vertical";
            default:
                return gravity.trim();
        }
    }

    private void addConstraints(Map<String, String> attrs, ComponentModel model) {
        putConstraint(attrs, "app:layout_constraintTop_toTopOf",
                model.getProperty("constraintTop"));
        putConstraint(attrs, "app:layout_constraintBottom_toBottomOf",
                model.getProperty("constraintBottom"));
        putConstraint(attrs, "app:layout_constraintStart_toStartOf",
                model.getProperty("constraintStart"));
        putConstraint(attrs, "app:layout_constraintEnd_toEndOf",
                model.getProperty("constraintEnd"));
        String biasH = model.getProperty("biasHorizontal");
        if (biasH != null) {
            attrs.put("app:layout_constraintHorizontal_bias", biasH.trim());
        }
        String biasV = model.getProperty("biasVertical");
        if (biasV != null) {
            attrs.put("app:layout_constraintVertical_bias", biasV.trim());
        }
    }

    private void putConstraint(Map<String, String> attrs, String key,
                               String target) {
        if (target == null || "none".equals(target)) {
            return;
        }
        if ("parent".equals(target)) {
            attrs.put(key, "parent");
        } else {
            attrs.put(key, "@id/" + XMLIdResolver.sanitizeId(target));
        }
    }

    private void putMargin(Map<String, String> attrs, String key, int dp) {
        if (dp > 0) {
            attrs.put(key, dp + "dp");
        }
    }

    /** Component attributes (text, colours, image source, ...). */
    private void addComponentAttributes(XMLDocumentWriter writer,
                                        ComponentModel model,
                                        Map<String, String> attrs) {
        List<String> unsupported = new ArrayList<>();
        Map<String, String> mapped = attributeMapper.map(model, unsupported);
        for (Map.Entry<String, String> entry : mapped.entrySet()) {
            attrs.put(entry.getKey(), entry.getValue());
        }
        for (String key : unsupported) {
            context.addWarning("xml", null, model.getId(),
                    "property '" + key + "' has no XML mapping — omitted");
        }
        // Image source: drawable resource, project asset, or omitted
        // with a warning (the validator already flagged it).
        if ("ImageView".equals(model.getType())) {
            String src = model.getProperty("src");
            if (src != null && !src.isEmpty()) {
                String drawable = resolveDrawable(src);
                if (drawable != null) {
                    attrs.put("android:src", "@drawable/" + drawable);
                } else {
                    context.addWarning("xml", null, model.getId(),
                            "image '" + src + "' could not be resolved — "
                                    + "android:src omitted");
                }
            }
        }
        // ProgressBar style is an XML attribute, not a property.
        if ("ProgressBar".equals(model.getType())) {
            String max = model.getProperty("max");
            String progress = model.getProperty("progress");
            if (max != null || progress != null) {
                attrs.put("style",
                        "?android:attr/progressBarStyleHorizontal");
            }
        }
    }

    /** Drawable resource name for an image src, or null. */
    private String resolveDrawable(String src) {
        if (src.startsWith("@drawable/")) {
            return src.substring("@drawable/".length());
        }
        if (context.getAssetRepository() != null) {
            for (com.nobi.appbuilder.database.AssetRepository.Asset asset
                    : context.getAssetRepository().getByProject(
                            context.getProject().getId())) {
                if (src.equals(asset.id) || src.equals(asset.name)) {
                    String name = asset.name;
                    int dot = name.lastIndexOf('.');
                    if (dot > 0) {
                        name = name.substring(0, dot);
                    }
                    return GenerationContext.ResourceCollector
                            .sanitizeResourceName(name);
                }
            }
        }
        // A bare drawable name (no path, no extension) is a resource.
        if (!src.contains("/") && !src.contains(".")) {
            return GenerationContext.ResourceCollector.sanitizeResourceName(src);
        }
        return null;
    }
}
