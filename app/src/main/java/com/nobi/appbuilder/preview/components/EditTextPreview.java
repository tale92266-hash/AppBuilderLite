package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.widget.EditText;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/** Real android.widget.EditText: real typing, hint, input type. */
public final class EditTextPreview {

    private EditTextPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             EditText view, PreviewContext previewContext) {
        PreviewStyleResolver.applyText(view, model,
                previewContext.getThemeManager());
        PreviewStyleResolver.applyBackground(view, model,
                previewContext.getThemeManager());
        String hint = model.getProperty("hint");
        if (hint != null) {
            view.setHint(hint);
        }
        view.setInputType(PreviewStyleResolver.inputTypeFor(
                model.getProperty("inputType")));
        // Restore a value typed earlier in this session (e.g. after a
        // theme change re-rendered the screen).
        String saved = previewContext.getSession().getInputValue(model.getId());
        if (saved != null) {
            view.setText(saved);
        }
        view.setSingleLine(!"multiline".equals(model.getProperty("inputType")));
    }
}
