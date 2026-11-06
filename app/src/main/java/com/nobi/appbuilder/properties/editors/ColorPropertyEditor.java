package com.nobi.appbuilder.properties.editors;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.nobi.appbuilder.properties.PropertyController;
import com.nobi.appbuilder.properties.PropertyDefinition;
import com.nobi.appbuilder.properties.PropertyValue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * HEX color editor: a swatch preview, a #RRGGBB (or #AARRGGBB) text
 * field, a preset palette and recent colors (kept in SharedPreferences).
 * Palette taps apply immediately; typed values commit on Done/focus loss.
 */
public class ColorPropertyEditor implements PropertyController.Editor {

    private static final String PREFS = "appbuilder_prefs";
    private static final String KEY_RECENT = "recent_colors";
    private static final int MAX_RECENT = 8;
    private static final int[] PRESETS = {
            0xFF0B4499, 0xFF7C5CFF, 0xFF111827, 0xFFFFFFFF, 0xFFEF4444,
            0xFFF59E0B, 0xFF10B981, 0xFF3B82F6, 0xFFEC4899, 0xFF6B7280
    };

    private final PropertyController.ValueListener listener;
    private View swatch;
    private EditText hexInput;
    private String currentHex;
    private boolean suppressEvents;

    public ColorPropertyEditor(PropertyDefinition definition, PropertyValue initialValue,
                               PropertyController.ValueListener listener) {
        this.listener = listener;
        this.currentHex = initialValue == null ? "" : initialValue.asString();
    }

    @Override
    public View createRow(Context context) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout top = new LinearLayout(context);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        swatch = new View(context);
        int swatchSize = (int) (36 * context.getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams swatchParams = new LinearLayout.LayoutParams(
                swatchSize, swatchSize);
        swatchParams.setMargins(0, 0, 8, 0);
        swatch.setLayoutParams(swatchParams);
        swatch.setBackgroundColor(parseOrTransparent(currentHex));
        hexInput = new EditText(context);
        hexInput.setLayoutParams(new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        hexInput.setInputType(InputType.TYPE_CLASS_TEXT);
        hexInput.setSingleLine(true);
        hexInput.setImeOptions(EditorInfo.IME_ACTION_DONE);
        hexInput.setHint("#0B4499");
        hexInput.setText(currentHex);
        hexInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                commitHex();
                return true;
            }
            return false;
        });
        hexInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                commitHex();
            }
        });
        top.addView(swatch);
        top.addView(hexInput);
        root.addView(top);
        root.addView(buildPalette(context, PRESETS));
        List<Integer> recent = loadRecent(context);
        if (!recent.isEmpty()) {
            int[] recentArray = new int[recent.size()];
            for (int i = 0; i < recent.size(); i++) {
                recentArray[i] = recent.get(i);
            }
            root.addView(buildPalette(context, recentArray));
        }
        return root;
    }

    private View buildPalette(Context context, int[] colors) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 8, 0, 0);
        float density = context.getResources().getDisplayMetrics().density;
        int size = (int) (28 * density);
        for (int color : colors) {
            View dot = new View(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(0, 0, 8, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundColor(color);
            dot.setOnClickListener(v -> applyColor(String.format("#%06X", 0xFFFFFF & color)));
            row.addView(dot);
        }
        return row;
    }

    private void commitHex() {
        if (suppressEvents) {
            return;
        }
        String hex = hexInput.getText().toString().trim();
        if (hex.isEmpty()) {
            listener.onValue(PropertyValue.ofColor(""));
            return;
        }
        if (!hex.startsWith("#")) {
            hex = "#" + hex;
        }
        applyColor(hex);
    }

    private void applyColor(String hex) {
        currentHex = hex;
        if (swatch != null) {
            swatch.setBackgroundColor(parseOrTransparent(hex));
        }
        if (hexInput != null && !hex.equals(hexInput.getText().toString())) {
            suppressEvents = true;
            hexInput.setText(hex);
            suppressEvents = false;
        }
        listener.onValue(PropertyValue.ofColor(hex));
        if (hexInput != null) {
            rememberRecent(hexInput.getContext(), hex);
        }
    }

    private int parseOrTransparent(String hex) {
        try {
            return Color.parseColor(hex);
        } catch (IllegalArgumentException e) {
            return Color.TRANSPARENT;
        }
    }

    private List<Integer> loadRecent(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_RECENT, "");
        List<Integer> out = new ArrayList<>();
        for (String part : saved.split(",")) {
            if (part.trim().isEmpty()) {
                continue;
            }
            try {
                out.add(Color.parseColor(part.trim()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        return out;
    }

    private void rememberRecent(Context context, String hex) {
        try {
            int color = Color.parseColor(hex);
            LinkedHashSet<String> set = new LinkedHashSet<>();
            set.add(String.format("#%06X", 0xFFFFFF & color));
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            for (String part : prefs.getString(KEY_RECENT, "").split(",")) {
                if (!part.trim().isEmpty()) {
                    set.add(part.trim());
                }
            }
            List<String> list = new ArrayList<>(set);
            while (list.size() > MAX_RECENT) {
                list.remove(list.size() - 1);
            }
            StringBuilder joined = new StringBuilder();
            for (String item : list) {
                if (joined.length() > 0) {
                    joined.append(',');
                }
                joined.append(item);
            }
            prefs.edit().putString(KEY_RECENT, joined.toString()).apply();
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public void setValue(PropertyValue value) {
        String hex = value == null ? "" : value.asString();
        currentHex = hex;
        if (swatch != null) {
            swatch.setBackgroundColor(parseOrTransparent(hex));
        }
        if (hexInput != null && !hex.equals(hexInput.getText().toString())) {
            suppressEvents = true;
            hexInput.setText(hex);
            suppressEvents = false;
        }
    }
}
