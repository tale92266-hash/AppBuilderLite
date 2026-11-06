package com.nobi.appbuilder.properties.editors;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

import com.nobi.appbuilder.properties.PropertyController;
import com.nobi.appbuilder.properties.PropertyDefinition;
import com.nobi.appbuilder.properties.PropertyValue;

/**
 * Resource-reference editor: a text field for values like
 * @drawable/ic_launcher plus a preset list (None and the drawables
 * shipped with the app). The reference is stored as written; Phase 7
 * resolves it when generating XML.
 */
public class ResourcePropertyEditor implements PropertyController.Editor {

    private static final String[] PRESETS = {
            "",
            "@drawable/ic_launcher_background",
            "@drawable/ic_folder",
            "@drawable/ic_add",
            "@mipmap/ic_launcher"
    };

    private final PropertyController.ValueListener listener;
    private EditText editText;
    private String currentValue;
    private boolean suppressEvents;

    public ResourcePropertyEditor(PropertyDefinition definition, PropertyValue initialValue,
                                  PropertyController.ValueListener listener) {
        this.listener = listener;
        this.currentValue = initialValue == null ? "" : initialValue.asString();
    }

    @Override
    public View createRow(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        editText.setInputType(InputType.TYPE_CLASS_TEXT);
        editText.setSingleLine(true);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setHint("@drawable/...");
        editText.setText(currentValue);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                commit();
                return true;
            }
            return false;
        });
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                commit();
            }
        });
        Button pickButton = new Button(context);
        pickButton.setText("Pick");
        pickButton.setOnClickListener(v -> {
            String[] labels = {"None", "Launcher background", "Folder icon",
                    "Add icon", "Launcher icon"};
            new AlertDialog.Builder(context)
                    .setTitle("Choose resource")
                    .setItems(labels, (dialog, which) -> {
                        setText(PRESETS[which]);
                        commit();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
        root.addView(editText);
        root.addView(pickButton);
        return root;
    }

    private void setText(String text) {
        currentValue = text;
        if (editText != null) {
            suppressEvents = true;
            editText.setText(text);
            suppressEvents = false;
        }
    }

    private void commit() {
        if (suppressEvents || editText == null) {
            return;
        }
        currentValue = editText.getText().toString().trim();
        listener.onValue(PropertyValue.ofString(currentValue));
    }

    @Override
    public void setValue(PropertyValue value) {
        String text = value == null ? "" : value.asString();
        currentValue = text;
        if (editText != null && !text.equals(editText.getText().toString())) {
            suppressEvents = true;
            editText.setText(text);
            suppressEvents = false;
        }
    }
}
