package com.nobi.appbuilder.generator.resources;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.templates.ResourceTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Shape drawables: one bg_&lt;componentId&gt;.xml per component that
 * asked for a rounded/bordered background (registered by the XML
 * stage), plus the documented template defaults bg_button.xml and
 * bg_card.xml.
 */
public class DrawableGenerator {

    private final GenerationContext context;

    public DrawableGenerator(GenerationContext context) {
        this.context = context;
    }

    /** file name (without path) -> XML content. */
    public Map<String, String> generate() {
        Map<String, String> files = new LinkedHashMap<>();
        GenerationContext.ResourceCollector collector = context.getResources();
        for (Map.Entry<String, GenerationContext.ResourceCollector.ShapeSpec> entry
                : collector.shapeSpecs.entrySet()) {
            GenerationContext.ResourceCollector.ShapeSpec spec = entry.getValue();
            String colorRef = colorReference(spec.color);
            String fileName = collector.shapes.get(entry.getKey());
            if (fileName == null) {
                continue;
            }
            // Shape names are registered without an extension (they
            // double as the @drawable reference name), but the
            // generated file must end in .xml — without it AAPT will
            // not compile it and the cross-reference check does not
            // see it as a drawable at all, failing the export.
            if (!fileName.endsWith(".xml")) {
                fileName = fileName + ".xml";
            }
            String radius = spec.radiusDp > 0 ? dp(spec.radiusDp) : null;
            String stroke = spec.borderWidthDp > 0 && spec.borderColor != null
                    ? dp(spec.borderWidthDp) : null;
            files.put(fileName, ResourceTemplate.componentShapeXml(
                    colorRef, radius, stroke, spec.borderColor));
        }
        // Register the template colours so the @color references
        // below always resolve (colorFor returns the existing name
        // when the hex was already collected under another name).
        String primaryRef = "@color/" + collector.colorFor("#0B4499",
                "color_primary");
        String surfaceRef = "@color/" + collector.colorFor("#FFFFFF",
                "color_surface");
        files.put("bg_button.xml",
                ResourceTemplate.shapeXml(primaryRef, "8dp"));
        files.put("bg_card.xml",
                ResourceTemplate.shapeXml(surfaceRef, "12dp"));
        return files;
    }

    /** "8dp" for 8, "8.5dp" for 8.5 — no trailing ".0". */
    private static String dp(float value) {
        if (value == (int) value) {
            return (int) value + "dp";
        }
        return value + "dp";
    }

    /** Hex that was deduplicated into @color -> its reference. */
    private String colorReference(String hex) {
        if (hex == null) {
            return "#FFFFFF";
        }
        String name = context.getResources().colors.get(hex.toUpperCase());
        return name == null ? hex : "@color/" + name;
    }
}
