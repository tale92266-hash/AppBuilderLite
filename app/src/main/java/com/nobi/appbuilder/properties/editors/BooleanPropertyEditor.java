package com.nobi.appbuilder.properties.editors;

import android.content.Context;
import android.view.View;

import androidx.appcompat.widget.SwitchCompat;

import com.nobi.appbuilder.properties.PropertyController;
import com.nobi.appbuilder.properties.PropertyDefinition;
import com.nobi.appbuilder.properties.PropertyValue;

/** Native switch editor; toggles apply immediately. */
public class BooleanPropertyEditor implements PropertyController.Editor {

    private final PropertyController.ValueListener listener;
    private SwitchCompat switchView;
    private boolean currentValue;
    private boolean suppressEvents;

    public BooleanPropertyEditor(PropertyDefinition definition, PropertyValue initialValue,
                                 PropertyController.ValueListener listener) {
        this.listener = listener;
        this.currentValue = initialValue != null && initialValue.asBoolean(false);
    }

    @Override
    public View createRow(Context context) {
        switchView = new SwitchCompat(context);
        switchView.setChecked(currentValue);
        switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressEvents) {
                return;
            }
            currentValue = isChecked;
            listener.onValue(PropertyValue.ofBoolean(isChecked));
        });
        return switchView;
    }

    @Override
    public void setValue(PropertyValue value) {
        if (value == null) {
            return;
        }
        currentValue = value.asBoolean(false);
        if (switchView != null && switchView.isChecked() != currentValue) {
            suppressEvents = true;
            switchView.setChecked(currentValue);
            suppressEvents = false;
        }
    }
}
