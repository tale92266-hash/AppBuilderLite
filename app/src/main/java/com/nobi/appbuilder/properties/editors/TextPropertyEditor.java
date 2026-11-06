package com.nobi.appbuilder.properties.editors;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import com.nobi.appbuilder.properties.PropertyController;
import com.nobi.appbuilder.properties.PropertyDefinition;
import com.nobi.appbuilder.properties.PropertyValue;

/**
 * Single-line or multiline text editor with debounced live updates
 * (300 ms after the last keystroke) and an immediate commit on Done
 * or focus loss. Max length is enforced by the validator.
 */
public class TextPropertyEditor implements PropertyController.Editor {

    private static final long DEBOUNCE_MS = 300;

    private final PropertyDefinition definition;
    private final PropertyController.ValueListener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable commitRunnable;
    private EditText editText;
    private String pendingText;
    private boolean suppressEvents;

    public TextPropertyEditor(PropertyDefinition definition, PropertyValue initialValue,
                              PropertyController.ValueListener listener) {
        this.definition = definition;
        this.listener = listener;
        this.pendingText = initialValue == null ? "" : initialValue.asString();
        this.commitRunnable = () -> {
            if (editText != null && pendingText != null) {
                listener.onValue(PropertyValue.ofString(pendingText));
            }
        };
    }

    @Override
    public View createRow(Context context) {
        editText = new EditText(context);
        boolean multiline = "text".equals(definition.getKey())
                || "hint".equals(definition.getKey());
        if (multiline) {
            editText.setInputType(InputType.TYPE_CLASS_TEXT
                    | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            editText.setMinLines(2);
            editText.setMaxLines(4);
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT);
            editText.setSingleLine(true);
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        editText.setText(pendingText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (suppressEvents) {
                    return;
                }
                pendingText = s.toString();
                handler.removeCallbacks(commitRunnable);
                handler.postDelayed(commitRunnable, DEBOUNCE_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handler.removeCallbacks(commitRunnable);
                commitRunnable.run();
                return true;
            }
            return false;
        });
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                handler.removeCallbacks(commitRunnable);
                commitRunnable.run();
            }
        });
        return editText;
    }

    @Override
    public void setValue(PropertyValue value) {
        String text = value == null ? "" : value.asString();
        if (editText != null && !text.equals(editText.getText().toString())) {
            suppressEvents = true;
            editText.setText(text);
            editText.setSelection(Math.min(text.length(), editText.getText().length()));
            suppressEvents = false;
        }
        pendingText = text;
    }
}
