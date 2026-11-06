package com.nobi.appbuilder.properties.editors;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.nobi.appbuilder.properties.PropertyController;
import com.nobi.appbuilder.properties.PropertyDefinition;
import com.nobi.appbuilder.properties.PropertyValue;

/**
 * Number editor with increment/decrement buttons and min/max clamping.
 * Buttons apply immediately; typed values commit on Done or focus loss.
 */
public class NumberPropertyEditor implements PropertyController.Editor {

    private final PropertyDefinition definition;
    private final PropertyController.ValueListener listener;
    private final boolean isFloat;
    private EditText editText;
    private float currentValue;
    private boolean suppressEvents;

    public NumberPropertyEditor(PropertyDefinition definition, PropertyValue initialValue,
                                PropertyController.ValueListener listener) {
        this.definition = definition;
        this.listener = listener;
        this.isFloat = definition.getType()
                == com.nobi.appbuilder.properties.PropertyType.FLOAT;
        this.currentValue = initialValue == null ? 0
                : initialValue.asFloat(definition.getMinValue() == Float.MIN_VALUE
                        ? 0 : definition.getMinValue());
    }

    @Override
    public View createRow(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Button minus = new Button(context);
        minus.setText("−");
        minus.setOnClickListener(v -> step(isFloat ? -0.5f : -1));
        editText = new EditText(context);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        editText.setInputType(isFloat
                ? InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL
                : InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_SIGNED);
        editText.setSingleLine(true);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setText(format(currentValue));
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                commitTyped();
                return true;
            }
            return false;
        });
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                commitTyped();
            }
        });
        Button plus = new Button(context);
        plus.setText("+");
        plus.setOnClickListener(v -> step(isFloat ? 0.5f : 1));
        root.addView(minus);
        root.addView(editText);
        root.addView(plus);
        return root;
    }

    private void step(float delta) {
        apply(currentValue + delta);
    }

    private void commitTyped() {
        if (suppressEvents || editText == null) {
            return;
        }
        try {
            apply(Float.parseFloat(editText.getText().toString().trim()));
        } catch (NumberFormatException e) {
            refreshText();
        }
    }

    private void apply(float raw) {
        float clamped = Math.max(definition.getMinValue(),
                Math.min(definition.getMaxValue(), raw));
        if (clamped == currentValue) {
            refreshText();
            return;
        }
        currentValue = clamped;
        refreshText();
        listener.onValue(isFloat
                ? PropertyValue.ofFloat(currentValue)
                : PropertyValue.ofInt(Math.round(currentValue)));
    }

    private void refreshText() {
        if (editText == null) {
            return;
        }
        String text = format(currentValue);
        if (!text.equals(editText.getText().toString())) {
            suppressEvents = true;
            editText.setText(text);
            suppressEvents = false;
        }
    }

    private String format(float value) {
        if (!isFloat || value == Math.round(value)) {
            return String.valueOf(Math.round(value));
        }
        return String.valueOf(value);
    }

    @Override
    public void setValue(PropertyValue value) {
        if (value == null) {
            return;
        }
        currentValue = value.asFloat(currentValue);
        refreshText();
    }
}
