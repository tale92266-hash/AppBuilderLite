package com.nobi.appbuilder.preview.factory;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/**
 * Creates the raw native Android View for a component type — nothing
 * else. Property application lives in the components/*Preview
 * classes, layout params in PreviewLayoutFactory. Returns null for
 * an unknown type so the caller can render an error placeholder.
 */
public class PreviewViewFactory {

    public View create(Context context, ComponentModel model) {
        String type = model.getType();
        if (type == null) {
            return null;
        }
        switch (type) {
            case "TextView":
                return new TextView(context);
            case "Button":
                return new MaterialButton(context);
            case "ImageView":
                return new ImageView(context);
            case "EditText":
                return new EditText(context);
            case "LinearLayout":
                return new LinearLayout(context);
            case "FrameLayout":
                return new FrameLayout(context);
            case "ConstraintLayout":
                return new ConstraintLayout(context);
            case "ScrollView":
                return new ScrollView(context);
            case "CheckBox":
                return new CheckBox(context);
            case "Switch":
                return new Switch(context);
            case "ProgressBar": {
                // Horizontal bar when the design carries progress data,
                // circular spinner otherwise (style is constructor-time).
                String max = model.getProperty("max");
                String progress = model.getProperty("progress");
                if (max != null || progress != null) {
                    return new ProgressBar(context, null,
                            android.R.attr.progressBarStyleHorizontal);
                }
                return new ProgressBar(context);
            }
            case "MaterialCardView":
                return new MaterialCardView(context);
            default:
                return null;
        }
    }

    /** Whether a type can hold children in the preview tree. */
    public static boolean isContainer(String type) {
        return "LinearLayout".equals(type)
                || "FrameLayout".equals(type)
                || "ConstraintLayout".equals(type)
                || "ScrollView".equals(type)
                || "MaterialCardView".equals(type);
    }
}
