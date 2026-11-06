package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.LayoutModel;
import com.nobi.appbuilder.utils.DimensionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One component type in the registry: what it is, what it looks like by
 * default, where it may live, and the three strategies that make it real —
 * canvas rendering (editor), preview creation (Phase 6) and XML attributes
 * (Phase 7). Nothing in the app switches on a component type string
 * except the registry itself.
 */
public class ComponentDefinition {

    /** Draws the component inside bounds on the design canvas. */
    public interface CanvasRenderer {
        void draw(Canvas canvas, RectF bounds, ComponentModel model, float density);
    }

    /** Builds the real Android View shown in the preview renderer. */
    public interface PreviewBuilder {
        View build(Context context, ComponentModel model);
    }

    /** Maps a component to its android: XML attributes. */
    public interface XmlAttributesProvider {
        Map<String, String> attributes(ComponentModel model);
    }

    private final String type;
    private final String displayName;
    private final ComponentCategory category;
    private final int defaultWidthDp;
    private final int defaultHeightDp;
    private final boolean container;
    private final Map<String, String> defaultProperties;
    private final List<String> supportedParents;
    private final int minWidthDp;
    private final int minHeightDp;
    private final CanvasRenderer canvasRenderer;
    private final PreviewBuilder previewBuilder;
    private final XmlAttributesProvider xmlProvider;

    private ComponentDefinition(Builder builder) {
        type = builder.type;
        displayName = builder.displayName;
        category = builder.category;
        defaultWidthDp = builder.defaultWidthDp;
        defaultHeightDp = builder.defaultHeightDp;
        container = builder.container;
        defaultProperties = builder.defaultProperties;
        supportedParents = builder.supportedParents;
        minWidthDp = builder.minWidthDp;
        minHeightDp = builder.minHeightDp;
        canvasRenderer = builder.canvasRenderer;
        previewBuilder = builder.previewBuilder;
        xmlProvider = builder.xmlProvider;
    }

    public String getType() {
        return type;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ComponentCategory getCategory() {
        return category;
    }

    public int getDefaultWidthDp() {
        return defaultWidthDp;
    }

    public int getDefaultHeightDp() {
        return defaultHeightDp;
    }

    public boolean isContainer() {
        return container;
    }

    /** A fresh copy of the default properties (never the shared map). */
    public Map<String, String> createDefaultProperties() {
        return new LinkedHashMap<>(defaultProperties);
    }

    public Map<String, String> getDefaultProperties() {
        return new LinkedHashMap<>(defaultProperties);
    }

    /**
     * Parent types this component may be dropped into. Empty means any
     * container may hold it.
     */
    public List<String> getSupportedParents() {
        return new ArrayList<>(supportedParents);
    }

    public boolean supportsParent(String parentType) {
        return supportedParents.isEmpty() || supportedParents.contains(parentType);
    }

    public int getMinWidthDp() {
        return minWidthDp;
    }

    public int getMinHeightDp() {
        return minHeightDp;
    }

    public CanvasRenderer getCanvasRenderer() {
        return canvasRenderer;
    }

    public PreviewBuilder getPreviewBuilder() {
        return previewBuilder;
    }

    public XmlAttributesProvider getXmlProvider() {
        return xmlProvider;
    }

    /** Parses "#RRGGBB" safely; returns fallback for anything else. */
    public static int parseColor(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Color.parseColor(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    /** Parses a gravity word ("left", "center", "right", ...) to Gravity. */
    public static int parseGravity(String value) {
        if (value == null) {
            return Gravity.START | Gravity.CENTER_VERTICAL;
        }
        String v = value.trim().toLowerCase();
        switch (v) {
            case "center":
                return Gravity.CENTER;
            case "right":
            case "end":
                return Gravity.END | Gravity.CENTER_VERTICAL;
            case "left":
            case "start":
                return Gravity.START | Gravity.CENTER_VERTICAL;
            case "top":
                return Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            case "bottom":
                return Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            case "center_horizontal":
                return Gravity.CENTER_HORIZONTAL;
            case "center_vertical":
                return Gravity.CENTER_VERTICAL;
            default:
                return Gravity.START | Gravity.CENTER_VERTICAL;
        }
    }

    /** LayoutParams for a preview View from the model's dp size. */
    public static ViewGroup.LayoutParams layoutParams(Context context, ComponentModel model) {
        int width = model.getWidth();
        int height = model.getHeight();
        int w = width == ComponentModel.MATCH_PARENT
                ? ViewGroup.LayoutParams.MATCH_PARENT
                : width == ComponentModel.WRAP_CONTENT
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : DimensionUtils.dpToPx(context, width);
        int h = height == ComponentModel.MATCH_PARENT
                ? ViewGroup.LayoutParams.MATCH_PARENT
                : height == ComponentModel.WRAP_CONTENT
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : DimensionUtils.dpToPx(context, height);
        return new ViewGroup.LayoutParams(w, h);
    }

    /** Base android:layout_width / layout_height attributes for XML output. */
    public static Map<String, String> baseXmlAttributes(ComponentModel model) {
        Map<String, String> attrs = new LinkedHashMap<>();
        attrs.put("android:layout_width", LayoutModel.dimensionToString(model.getWidth()));
        attrs.put("android:layout_height", LayoutModel.dimensionToString(model.getHeight()));
        return attrs;
    }

    /** Text size in sp from properties, or fallback. */
    public static float parseTextSizeSp(ComponentModel model, float fallback) {
        String raw = model.getProperty("textSize");
        if (raw == null) {
            return fallback;
        }
        try {
            float size = Float.parseFloat(raw.trim());
            return size > 0 ? size : fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static class Builder {
        private final String type;
        private final String displayName;
        private final ComponentCategory category;
        private int defaultWidthDp = ComponentModel.WRAP_CONTENT;
        private int defaultHeightDp = ComponentModel.WRAP_CONTENT;
        private boolean container;
        private final Map<String, String> defaultProperties = new LinkedHashMap<>();
        private final List<String> supportedParents = new ArrayList<>();
        private int minWidthDp;
        private int minHeightDp;
        private CanvasRenderer canvasRenderer;
        private PreviewBuilder previewBuilder;
        private XmlAttributesProvider xmlProvider;

        public Builder(String type, String displayName, ComponentCategory category) {
            this.type = type;
            this.displayName = displayName;
            this.category = category;
        }

        public Builder defaultSize(int widthDp, int heightDp) {
            defaultWidthDp = widthDp;
            defaultHeightDp = heightDp;
            return this;
        }

        public Builder container(boolean container) {
            this.container = container;
            return this;
        }

        public Builder defaultProperty(String key, String value) {
            defaultProperties.put(key, value);
            return this;
        }

        public Builder supportedParent(String... parents) {
            for (String parent : parents) {
                supportedParents.add(parent);
            }
            return this;
        }

        public Builder minSize(int widthDp, int heightDp) {
            minWidthDp = widthDp;
            minHeightDp = heightDp;
            return this;
        }

        public Builder canvasRenderer(CanvasRenderer renderer) {
            canvasRenderer = renderer;
            return this;
        }

        public Builder previewBuilder(PreviewBuilder builder) {
            previewBuilder = builder;
            return this;
        }

        public Builder xmlProvider(XmlAttributesProvider provider) {
            xmlProvider = provider;
            return this;
        }

        public ComponentDefinition build() {
            return new ComponentDefinition(this);
        }
    }
}
