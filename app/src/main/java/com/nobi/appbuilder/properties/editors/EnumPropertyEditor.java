package com.nobi.appbuilder.properties.editors;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.nobi.appbuilder.properties.PropertyController;
import com.nobi.appbuilder.properties.PropertyDefinition;
import com.nobi.appbuilder.properties.PropertyValue;

import java.util.List;

/** Dropdown editor for ENUM / GRAVITY / ALIGNMENT properties. */
public class EnumPropertyEditor implements PropertyController.Editor {

    private final PropertyDefinition definition;
    private final PropertyController.ValueListener listener;
    private Spinner spinner;
    private String currentValue;
    private boolean suppressEvents;
    private boolean firstSelection = true;

    public EnumPropertyEditor(PropertyDefinition definition, PropertyValue initialValue,
                              PropertyController.ValueListener listener) {
        this.definition = definition;
        this.listener = listener;
        this.currentValue = initialValue == null ? "" : initialValue.asString();
    }

    @Override
    public View createRow(Context context) {
        spinner = new Spinner(context);
        spinner.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        List<String> values = definition.getAllowedValues();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        int selected = Math.max(0, values.indexOf(currentValue));
        spinner.setSelection(selected);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (firstSelection) {
                    firstSelection = false;
                    return;
                }
                if (suppressEvents) {
                    return;
                }
                String value = values.get(position);
                if (!value.equals(currentValue)) {
                    currentValue = value;
                    listener.onValue(PropertyValue.ofEnum(value));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return spinner;
    }

    @Override
    public void setValue(PropertyValue value) {
        if (value == null || spinner == null) {
            return;
        }
        String text = value.asString();
        currentValue = text;
        int index = definition.getAllowedValues().indexOf(text);
        if (index >= 0 && spinner.getSelectedItemPosition() != index) {
            suppressEvents = true;
            spinner.setSelection(index);
            suppressEvents = false;
        }
    }
}
