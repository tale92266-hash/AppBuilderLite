package com.nobi.appbuilder.properties.editors;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupMenu;

import com.nobi.appbuilder.properties.PropertyController;
import com.nobi.appbuilder.properties.PropertyDefinition;
import com.nobi.appbuilder.properties.PropertyValue;

/**
 * Dimension editor: dp/sp numbers plus match_parent / wrap_content via
 * a popup menu. Commits on Done or focus loss.
 */
public class DimensionPropertyEditor implements PropertyController.Editor {

    private final PropertyController.ValueListener listener;
    private EditText editText;
    private String currentValue;
    private boolean suppressEvents;

    public DimensionPropertyEditor(PropertyDefinition definition, PropertyValue initialValue,
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
        editText.setHint("120dp");
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
        Button menuButton = new Button(context);
        menuButton.setText("⋯");
        menuButton.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(context, menuButton);
            menu.getMenu().add("match_parent");
            menu.getMenu().add("wrap_content");
            menu.setOnMenuItemClickListener(item -> {
                setText(item.getTitle().toString());
                commit();
                return true;
            });
            menu.show();
        });
        root.addView(editText);
        root.addView(menuButton);
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
        listener.onValue(PropertyValue.ofDimension(currentValue));
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
