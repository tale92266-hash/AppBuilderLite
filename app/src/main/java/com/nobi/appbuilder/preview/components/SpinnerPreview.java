package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;

import java.util.Arrays;

/** Real android.widget.Spinner with the design text as its entry. */
public final class SpinnerPreview {

    private SpinnerPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             Spinner view, PreviewContext previewContext) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item,
                Arrays.asList(model.getProperty("text", "Select an option")));
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        view.setAdapter(adapter);
    }
}
