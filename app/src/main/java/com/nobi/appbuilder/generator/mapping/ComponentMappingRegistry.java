package com.nobi.appbuilder.generator.mapping;

import com.nobi.appbuilder.components.ComponentRegistry;

/**
 * Maps a design component type to its generated XML tag. Most
 * widgets use their simple Android class name; the two library
 * views need their fully qualified tag.
 */
public final class ComponentMappingRegistry {

    private ComponentMappingRegistry() {
    }

    /** XML tag for a component type, or null when unsupported. */
    public static String xmlTagFor(String type) {
        if (type == null || !ComponentRegistry.contains(type)) {
            return null;
        }
        switch (type) {
            case "ConstraintLayout":
                return "androidx.constraintlayout.widget.ConstraintLayout";
            case "MaterialCardView":
                return "com.google.android.material.card.MaterialCardView";
            case "Divider":
                // A divider is a plain View with a background color.
                return "View";
            default:
                return type;
        }
    }

    /** Java class used for findViewById bindings of this type. */
    public static String javaClassFor(String type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case "TextView":
                return "TextView";
            case "Button":
                return "Button";
            case "ImageView":
                return "ImageView";
            case "EditText":
                return "EditText";
            case "LinearLayout":
                return "LinearLayout";
            case "FrameLayout":
                return "FrameLayout";
            case "ConstraintLayout":
                return "ConstraintLayout";
            case "ScrollView":
                return "ScrollView";
            case "CheckBox":
                return "CheckBox";
            case "Switch":
                return "Switch";
            case "ProgressBar":
                return "ProgressBar";
            case "RadioButton":
                return "RadioButton";
            case "Spinner":
                return "Spinner";
            case "RatingBar":
                return "RatingBar";
            case "SeekBar":
                return "SeekBar";
            case "Divider":
                return "View";
            case "MaterialCardView":
                return "MaterialCardView";
            default:
                return null;
        }
    }

    /** Import line for a binding type, or null for android.widget. */
    public static String importFor(String javaClass) {
        if (javaClass == null) {
            return null;
        }
        switch (javaClass) {
            case "ConstraintLayout":
                return "import androidx.constraintlayout.widget.ConstraintLayout;";
            case "MaterialCardView":
                return "import com.google.android.material.card.MaterialCardView;";
            default:
                return null;
        }
    }

    public static boolean isSupported(String type) {
        return xmlTagFor(type) != null;
    }
}
