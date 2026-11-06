package com.nobi.appbuilder.properties;

import java.util.ArrayList;
import java.util.List;

/**
 * The property schema. Definitions are declared once here — never
 * hardcoded in the inspector — and each carries its Phase 7 XML
 * attribute mapping. Only definitions that apply to a component type
 * (and, for constraints, to its parent) are ever shown.
 */
public final class PropertyRegistry {

    private static final String[] TEXT_TYPES = {
            "TextView", "Button", "EditText", "CheckBox", "Switch"
    };
    private static final String[] CONTAINER_TYPES = {
            "LinearLayout", "FrameLayout", "ConstraintLayout", "ScrollView", "MaterialCardView"
    };

    private static final List<PropertyDefinition> ALL = new ArrayList<>();

    static {
        // ------------------------------------------------ General
        ALL.add(new PropertyDefinition.Builder("id", "Component ID",
                PropertyGroup.GENERAL, PropertyType.STRING)
                .defaultValue("")
                .xmlAttribute("android:id")
                .build());
        ALL.add(new PropertyDefinition.Builder("type", "Component type",
                PropertyGroup.GENERAL, PropertyType.STRING)
                .editable(false)
                .build());
        ALL.add(new PropertyDefinition.Builder("visibility", "Visibility",
                PropertyGroup.GENERAL, PropertyType.ENUM)
                .defaultValue("visible")
                .allowedValues("visible", "invisible", "gone")
                .xmlAttribute("android:visibility")
                .build());
        ALL.add(new PropertyDefinition.Builder("enabled", "Enabled",
                PropertyGroup.GENERAL, PropertyType.BOOLEAN)
                .defaultValue("true")
                .xmlAttribute("android:enabled")
                .build());
        ALL.add(new PropertyDefinition.Builder("clickable", "Clickable",
                PropertyGroup.GENERAL, PropertyType.BOOLEAN)
                .defaultValue("false")
                .xmlAttribute("android:clickable")
                .build());
        ALL.add(new PropertyDefinition.Builder("focusable", "Focusable",
                PropertyGroup.GENERAL, PropertyType.BOOLEAN)
                .defaultValue("false")
                .xmlAttribute("android:focusable")
                .build());
        ALL.add(new PropertyDefinition.Builder("contentDescription", "Content description",
                PropertyGroup.GENERAL, PropertyType.STRING)
                .defaultValue("")
                .xmlAttribute("android:contentDescription")
                .build());
        ALL.add(new PropertyDefinition.Builder("locked", "Locked in editor",
                PropertyGroup.GENERAL, PropertyType.BOOLEAN)
                .defaultValue("false")
                .build());

        // ------------------------------------------------ Layout
        ALL.add(new PropertyDefinition.Builder("width", "Width",
                PropertyGroup.LAYOUT, PropertyType.DIMENSION)
                .defaultValue("wrap_content")
                .affectsLayout(true)
                .xmlAttribute("android:layout_width")
                .build());
        ALL.add(new PropertyDefinition.Builder("height", "Height",
                PropertyGroup.LAYOUT, PropertyType.DIMENSION)
                .defaultValue("wrap_content")
                .affectsLayout(true)
                .xmlAttribute("android:layout_height")
                .build());
        ALL.add(new PropertyDefinition.Builder("x", "X position",
                PropertyGroup.LAYOUT, PropertyType.INTEGER)
                .defaultValue("0")
                .range(0, 4000)
                .affectsLayout(true)
                .build());
        ALL.add(new PropertyDefinition.Builder("y", "Y position",
                PropertyGroup.LAYOUT, PropertyType.INTEGER)
                .defaultValue("0")
                .range(0, 4000)
                .affectsLayout(true)
                .build());
        ALL.add(new PropertyDefinition.Builder("gravity", "Gravity",
                PropertyGroup.LAYOUT, PropertyType.GRAVITY)
                .defaultValue("")
                .allowedValues("", "left", "center", "right", "top", "bottom",
                        "center_horizontal", "center_vertical")
                .xmlAttribute("android:gravity")
                .build());
        ALL.add(new PropertyDefinition.Builder("layoutGravity", "Layout gravity",
                PropertyGroup.LAYOUT, PropertyType.GRAVITY)
                .defaultValue("")
                .allowedValues("", "left", "center", "right", "top", "bottom",
                        "center_horizontal", "center_vertical")
                .xmlAttribute("android:layout_gravity")
                .build());
        ALL.add(new PropertyDefinition.Builder("layoutWeight", "Layout weight",
                PropertyGroup.LAYOUT, PropertyType.FLOAT)
                .defaultValue("0")
                .range(0, 10)
                .affectsLayout(true)
                .xmlAttribute("android:layout_weight")
                .build());
        ALL.add(new PropertyDefinition.Builder("orientation", "Orientation",
                PropertyGroup.LAYOUT, PropertyType.ENUM)
                .defaultValue("vertical")
                .allowedValues("vertical", "horizontal")
                .affectsLayout(true)
                .xmlAttribute("android:orientation")
                .appliesTo("LinearLayout")
                .build());
        ALL.add(new PropertyDefinition.Builder("weightSum", "Weight sum",
                PropertyGroup.LAYOUT, PropertyType.FLOAT)
                .defaultValue("0")
                .range(0, 100)
                .xmlAttribute("android:weightSum")
                .appliesTo("LinearLayout")
                .build());
        ALL.add(new PropertyDefinition.Builder("fillViewport", "Fill viewport",
                PropertyGroup.LAYOUT, PropertyType.BOOLEAN)
                .defaultValue("false")
                .xmlAttribute("android:fillViewport")
                .appliesTo("ScrollView")
                .build());

        // ------------------------------------------------ Spacing
        addSpacing("marginLeft", "Margin left", "android:layout_marginLeft");
        addSpacing("marginTop", "Margin top", "android:layout_marginTop");
        addSpacing("marginRight", "Margin right", "android:layout_marginRight");
        addSpacing("marginBottom", "Margin bottom", "android:layout_marginBottom");
        addSpacing("paddingLeft", "Padding left", "android:paddingLeft");
        addSpacing("paddingTop", "Padding top", "android:paddingTop");
        addSpacing("paddingRight", "Padding right", "android:paddingRight");
        addSpacing("paddingBottom", "Padding bottom", "android:paddingBottom");

        // ------------------------------------------------ Appearance
        ALL.add(new PropertyDefinition.Builder("backgroundColor", "Background color",
                PropertyGroup.APPEARANCE, PropertyType.COLOR)
                .defaultValue("")
                .affectsAppearance(true)
                .xmlAttribute("android:background")
                .build());
        ALL.add(new PropertyDefinition.Builder("backgroundDrawable", "Background drawable",
                PropertyGroup.APPEARANCE, PropertyType.RESOURCE)
                .defaultValue("")
                .affectsAppearance(true)
                .xmlAttribute("android:background")
                .build());
        ALL.add(new PropertyDefinition.Builder("borderColor", "Border color",
                PropertyGroup.APPEARANCE, PropertyType.COLOR)
                .defaultValue("")
                .affectsAppearance(true)
                .build());
        ALL.add(new PropertyDefinition.Builder("borderWidth", "Border width",
                PropertyGroup.APPEARANCE, PropertyType.INTEGER)
                .defaultValue("0")
                .range(0, 32)
                .affectsAppearance(true)
                .build());
        ALL.add(new PropertyDefinition.Builder("cornerRadius", "Corner radius",
                PropertyGroup.APPEARANCE, PropertyType.DIMENSION)
                .defaultValue("0")
                .affectsAppearance(true)
                .build());
        ALL.add(new PropertyDefinition.Builder("elevation", "Elevation",
                PropertyGroup.APPEARANCE, PropertyType.FLOAT)
                .defaultValue("0")
                .range(0, 24)
                .affectsAppearance(true)
                .xmlAttribute("android:elevation")
                .build());
        ALL.add(new PropertyDefinition.Builder("alpha", "Alpha",
                PropertyGroup.APPEARANCE, PropertyType.FLOAT)
                .defaultValue("1")
                .range(0, 1)
                .affectsAppearance(true)
                .xmlAttribute("android:alpha")
                .build());

        // ------------------------------------------------ Typography
        ALL.add(new PropertyDefinition.Builder("text", "Text",
                PropertyGroup.TYPOGRAPHY, PropertyType.STRING)
                .defaultValue("")
                .affectsAppearance(true)
                .xmlAttribute("android:text")
                .appliesTo(TEXT_TYPES)
                .build());
        ALL.add(new PropertyDefinition.Builder("textColor", "Text color",
                PropertyGroup.TYPOGRAPHY, PropertyType.COLOR)
                .defaultValue("#111827")
                .affectsAppearance(true)
                .xmlAttribute("android:textColor")
                .appliesTo(TEXT_TYPES)
                .build());
        ALL.add(new PropertyDefinition.Builder("textSize", "Text size",
                PropertyGroup.TYPOGRAPHY, PropertyType.DIMENSION)
                .defaultValue("16sp")
                .affectsAppearance(true)
                .xmlAttribute("android:textSize")
                .appliesTo(TEXT_TYPES)
                .build());
        ALL.add(new PropertyDefinition.Builder("fontFamily", "Font family",
                PropertyGroup.TYPOGRAPHY, PropertyType.ENUM)
                .defaultValue("sans-serif")
                .allowedValues("sans-serif", "sans-serif-light", "sans-serif-medium",
                        "sans-serif-condensed", "monospace", "serif")
                .xmlAttribute("android:fontFamily")
                .appliesTo(TEXT_TYPES)
                .build());
        ALL.add(new PropertyDefinition.Builder("fontWeight", "Font weight",
                PropertyGroup.TYPOGRAPHY, PropertyType.ENUM)
                .defaultValue("normal")
                .allowedValues("normal", "bold")
                .xmlAttribute("android:textStyle")
                .appliesTo(TEXT_TYPES)
                .build());
        ALL.add(new PropertyDefinition.Builder("textAlignment", "Text alignment",
                PropertyGroup.TYPOGRAPHY, PropertyType.ENUM)
                .defaultValue("inherit")
                .allowedValues("inherit", "textStart", "textEnd", "center",
                        "viewStart", "viewEnd")
                .xmlAttribute("android:textAlignment")
                .appliesTo(TEXT_TYPES)
                .build());
        ALL.add(new PropertyDefinition.Builder("lineSpacing", "Line spacing",
                PropertyGroup.TYPOGRAPHY, PropertyType.FLOAT)
                .defaultValue("1")
                .range(0.5f, 3)
                .xmlAttribute("android:lineSpacingMultiplier")
                .appliesTo(TEXT_TYPES)
                .build());
        ALL.add(new PropertyDefinition.Builder("maxLines", "Max lines",
                PropertyGroup.TYPOGRAPHY, PropertyType.INTEGER)
                .defaultValue("0")
                .range(0, 100)
                .xmlAttribute("android:maxLines")
                .appliesTo(TEXT_TYPES)
                .build());
        ALL.add(new PropertyDefinition.Builder("hint", "Hint",
                PropertyGroup.TYPOGRAPHY, PropertyType.STRING)
                .defaultValue("")
                .xmlAttribute("android:hint")
                .appliesTo("EditText")
                .build());
        ALL.add(new PropertyDefinition.Builder("inputType", "Input type",
                PropertyGroup.TYPOGRAPHY, PropertyType.ENUM)
                .defaultValue("text")
                .allowedValues("text", "textEmailAddress", "textPassword",
                        "number", "phone", "textMultiLine")
                .xmlAttribute("android:inputType")
                .appliesTo("EditText")
                .build());

        // ------------------------------------------------ Type-specific
        ALL.add(new PropertyDefinition.Builder("src", "Image source",
                PropertyGroup.APPEARANCE, PropertyType.RESOURCE)
                .defaultValue("")
                .xmlAttribute("android:src")
                .appliesTo("ImageView")
                .build());
        ALL.add(new PropertyDefinition.Builder("scaleType", "Scale type",
                PropertyGroup.APPEARANCE, PropertyType.ENUM)
                .defaultValue("centerCrop")
                .allowedValues("centerCrop", "fitCenter", "center",
                        "centerInside", "fitXY")
                .xmlAttribute("android:scaleType")
                .appliesTo("ImageView")
                .build());
        ALL.add(new PropertyDefinition.Builder("checked", "Checked",
                PropertyGroup.GENERAL, PropertyType.BOOLEAN)
                .defaultValue("false")
                .xmlAttribute("android:checked")
                .appliesTo("CheckBox", "Switch")
                .build());
        ALL.add(new PropertyDefinition.Builder("max", "Max value",
                PropertyGroup.GENERAL, PropertyType.INTEGER)
                .defaultValue("100")
                .range(1, 10000)
                .xmlAttribute("android:max")
                .appliesTo("ProgressBar")
                .build());
        ALL.add(new PropertyDefinition.Builder("progress", "Progress",
                PropertyGroup.GENERAL, PropertyType.INTEGER)
                .defaultValue("0")
                .range(0, 10000)
                .xmlAttribute("android:progress")
                .appliesTo("ProgressBar")
                .build());
        ALL.add(new PropertyDefinition.Builder("indeterminate", "Indeterminate",
                PropertyGroup.GENERAL, PropertyType.BOOLEAN)
                .defaultValue("false")
                .xmlAttribute("android:indeterminate")
                .appliesTo("ProgressBar")
                .build());

        // ------------------------------------------------ Advanced
        ALL.add(new PropertyDefinition.Builder("minWidth", "Min width",
                PropertyGroup.ADVANCED, PropertyType.INTEGER)
                .defaultValue("0")
                .range(0, 4000)
                .xmlAttribute("android:minWidth")
                .build());
        ALL.add(new PropertyDefinition.Builder("minHeight", "Min height",
                PropertyGroup.ADVANCED, PropertyType.INTEGER)
                .defaultValue("0")
                .range(0, 4000)
                .xmlAttribute("android:minHeight")
                .build());
        ALL.add(new PropertyDefinition.Builder("maxWidth", "Max width",
                PropertyGroup.ADVANCED, PropertyType.INTEGER)
                .defaultValue("0")
                .range(0, 4000)
                .xmlAttribute("android:maxWidth")
                .build());
        ALL.add(new PropertyDefinition.Builder("maxHeight", "Max height",
                PropertyGroup.ADVANCED, PropertyType.INTEGER)
                .defaultValue("0")
                .range(0, 4000)
                .xmlAttribute("android:maxHeight")
                .build());
    }

    private static void addSpacing(String key, String displayName, String xmlAttribute) {
        ALL.add(new PropertyDefinition.Builder(key, displayName,
                PropertyGroup.SPACING, PropertyType.INTEGER)
                .defaultValue("0")
                .range(0, 400)
                .affectsLayout(true)
                .xmlAttribute(xmlAttribute)
                .build());
    }

    private PropertyRegistry() {
    }

    /** Constraint definitions, shown only for children of a ConstraintLayout. */
    private static List<PropertyDefinition> constraintDefinitions() {
        List<PropertyDefinition> defs = new ArrayList<>();
        defs.add(new PropertyDefinition.Builder("constraintTop", "Constraint top",
                PropertyGroup.ADVANCED, PropertyType.STRING)
                .defaultValue("none")
                .xmlAttribute("app:layout_constraintTop_toTopOf")
                .build());
        defs.add(new PropertyDefinition.Builder("constraintBottom", "Constraint bottom",
                PropertyGroup.ADVANCED, PropertyType.STRING)
                .defaultValue("none")
                .xmlAttribute("app:layout_constraintBottom_toBottomOf")
                .build());
        defs.add(new PropertyDefinition.Builder("constraintStart", "Constraint start",
                PropertyGroup.ADVANCED, PropertyType.STRING)
                .defaultValue("none")
                .xmlAttribute("app:layout_constraintStart_toStartOf")
                .build());
        defs.add(new PropertyDefinition.Builder("constraintEnd", "Constraint end",
                PropertyGroup.ADVANCED, PropertyType.STRING)
                .defaultValue("none")
                .xmlAttribute("app:layout_constraintEnd_toEndOf")
                .build());
        defs.add(new PropertyDefinition.Builder("biasHorizontal", "Horizontal bias",
                PropertyGroup.ADVANCED, PropertyType.FLOAT)
                .defaultValue("0.5")
                .range(0, 1)
                .xmlAttribute("app:layout_constraintHorizontal_bias")
                .build());
        defs.add(new PropertyDefinition.Builder("biasVertical", "Vertical bias",
                PropertyGroup.ADVANCED, PropertyType.FLOAT)
                .defaultValue("0.5")
                .range(0, 1)
                .xmlAttribute("app:layout_constraintVertical_bias")
                .build());
        return defs;
    }

    /** All definitions that apply to a component type (plus constraints). */
    public static List<PropertyDefinition> getDefinitionsFor(String componentType,
                                                             String parentType) {
        List<PropertyDefinition> out = new ArrayList<>();
        for (PropertyDefinition definition : ALL) {
            if (definition.appliesTo(componentType)) {
                out.add(definition);
            }
        }
        if ("ConstraintLayout".equals(parentType)
                && !"ConstraintLayout".equals(componentType)) {
            out.addAll(constraintDefinitions());
        }
        return out;
    }

    public static PropertyDefinition get(String key) {
        for (PropertyDefinition definition : ALL) {
            if (definition.getKey().equals(key)) {
                return definition;
            }
        }
        for (PropertyDefinition definition : constraintDefinitions()) {
            if (definition.getKey().equals(key)) {
                return definition;
            }
        }
        return null;
    }

    /** XML attribute mapping for Phase 7 reuse: key -> android attribute. */
    public static String xmlAttributeFor(String key) {
        PropertyDefinition definition = get(key);
        return definition == null ? null : definition.getXmlAttribute();
    }
}
