package com.nobi.appbuilder.preview.theme;

import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.LayoutModel;
import com.nobi.appbuilder.utils.DimensionUtils;

/**
 * Shared property application for every preview component: colours,
 * text, spacing, alpha, visibility, elevation and input types.
 * Every getter is null-safe and every number parse falls back, so a
 * missing or malformed property can never crash the preview.
 */
public final class PreviewStyleResolver {

    private PreviewStyleResolver() {
    }

    public static float parseFloat(ComponentModel model, String key, float fallback) {
        String raw = model.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return Float.parseFloat(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static int parseInt(ComponentModel model, String key, int fallback) {
        String raw = model.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static boolean parseBoolean(ComponentModel model, String key,
                                       boolean fallback) {
        String raw = model.getProperty(key);
        if (raw == null) {
            return fallback;
        }
        return "true".equalsIgnoreCase(raw.trim())
                || "1".equals(raw.trim());
    }

    /** Background colour with optional corner radius / border. */
    public static void applyBackground(View view, ComponentModel model,
                                       PreviewThemeManager themeManager) {
        String bg = model.getProperty("backgroundColor");
        if (bg == null) {
            return;
        }
        int color = PreviewColorResolver.resolveToken(bg,
                themeManager.getEffectiveTheme(), 0);
        float radiusDp = parseFloat(model, "cornerRadius", 0f);
        float borderDp = parseFloat(model, "borderWidth", 0f);
        if (radiusDp > 0 || borderDp > 0) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(color);
            if (radiusDp > 0) {
                drawable.setCornerRadius(DimensionUtils.dpToPx(
                        view.getContext(), radiusDp));
            }
            if (borderDp > 0) {
                String borderColor = model.getProperty("borderColor");
                int stroke = PreviewColorResolver.resolveToken(borderColor,
                        themeManager.getEffectiveTheme(), 0xFF888888);
                drawable.setStroke(DimensionUtils.dpToPx(view.getContext(), borderDp),
                        stroke);
            }
            view.setBackground(drawable);
        } else {
            view.setBackgroundColor(color);
        }
    }

    /** Text, size, colour, alignment, lines and typeface. */
    public static void applyText(TextView textView, ComponentModel model,
                                 PreviewThemeManager themeManager) {
        String text = model.getProperty("text");
        if (text != null) {
            textView.setText(text);
        }
        float sizeSp = ComponentDefinition.parseTextSizeSp(model, 16f);
        textView.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, sizeSp);
        String color = model.getProperty("textColor");
        if (color != null) {
            textView.setTextColor(PreviewColorResolver.resolveToken(color,
                    themeManager.getEffectiveTheme(),
                    themeManager.getTextColor()));
        } else {
            textView.setTextColor(themeManager.getTextColor());
        }
        String gravity = model.getProperty("gravity");
        if (gravity != null) {
            textView.setGravity(ComponentDefinition.parseGravity(gravity));
        }
        int maxLines = parseInt(model, "maxLines", -1);
        if (maxLines > 0) {
            textView.setMaxLines(maxLines);
        }
        String weight = model.getProperty("fontWeight");
        String typefaceName = model.getProperty("typeface");
        Typeface base = Typeface.SANS_SERIF;
        if ("monospace".equalsIgnoreCase(typefaceName)) {
            base = Typeface.MONOSPACE;
        } else if ("serif".equalsIgnoreCase(typefaceName)) {
            base = Typeface.SERIF;
        }
        int style = "bold".equalsIgnoreCase(weight)
                ? Typeface.BOLD : Typeface.NORMAL;
        textView.setTypeface(base, style);
    }

    /** Padding, alpha, visibility, enabled, content description. */
    public static void applyCommon(View view, ComponentModel model) {
        LayoutModel layout = model.getLayout();
        if (layout != null) {
            view.setPadding(
                    DimensionUtils.dpToPx(view.getContext(), layout.getPaddingLeft()),
                    DimensionUtils.dpToPx(view.getContext(), layout.getPaddingTop()),
                    DimensionUtils.dpToPx(view.getContext(), layout.getPaddingRight()),
                    DimensionUtils.dpToPx(view.getContext(), layout.getPaddingBottom()));
        }
        float alpha = parseFloat(model, "alpha", 1f);
        if (alpha < 0f) {
            alpha = 0f;
        }
        if (alpha > 1f) {
            alpha = 1f;
        }
        view.setAlpha(alpha);
        String visibility = model.getProperty("visibility");
        if ("invisible".equalsIgnoreCase(visibility)) {
            view.setVisibility(View.INVISIBLE);
        } else if ("gone".equalsIgnoreCase(visibility)) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
        if (model.getProperty("enabled") != null) {
            view.setEnabled(parseBoolean(model, "enabled", true));
        }
        String description = model.getProperty("contentDescription");
        if (description != null) {
            view.setContentDescription(description);
        }
        float elevation = parseFloat(model, "elevation", 0f);
        if (elevation > 0) {
            view.setElevation(DimensionUtils.dpToPx(view.getContext(), elevation));
        }
    }

    /** Maps the editor's inputType names to Android InputType flags. */
    public static int inputTypeFor(String inputType) {
        if (inputType == null) {
            return InputType.TYPE_CLASS_TEXT;
        }
        switch (inputType.trim().toLowerCase()) {
            case "password":
                return InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            case "number":
                return InputType.TYPE_CLASS_NUMBER;
            case "phone":
                return InputType.TYPE_CLASS_PHONE;
            case "email":
                return InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            case "multiline":
                return InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_MULTI_LINE;
            case "text":
            default:
                return InputType.TYPE_CLASS_TEXT;
        }
    }

    /** Maps the editor's scaleType names to ImageView.ScaleType. */
    public static android.widget.ImageView.ScaleType scaleTypeFor(String scaleType) {
        if (scaleType == null) {
            return android.widget.ImageView.ScaleType.CENTER_CROP;
        }
        switch (scaleType.trim()) {
            case "fitCenter":
                return android.widget.ImageView.ScaleType.FIT_CENTER;
            case "fitXY":
                return android.widget.ImageView.ScaleType.FIT_XY;
            case "centerInside":
                return android.widget.ImageView.ScaleType.CENTER_INSIDE;
            case "center":
                return android.widget.ImageView.ScaleType.CENTER;
            case "centerCrop":
            default:
                return android.widget.ImageView.ScaleType.CENTER_CROP;
        }
    }

    /** Editor gravity names ("left", "center", "right", "center_vertical"). */
    public static int gravityFor(String gravity) {
        if (gravity == null) {
            return Gravity.NO_GRAVITY;
        }
        return ComponentDefinition.parseGravity(gravity);
    }
}
