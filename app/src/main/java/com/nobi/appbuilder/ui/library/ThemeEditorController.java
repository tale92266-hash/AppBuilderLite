package com.nobi.appbuilder.ui.library;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.library.design.ThemeDefinition;
import com.nobi.appbuilder.library.design.ThemeValidator;

import java.util.List;

/** Theme editor dialog: name, dark mode, six colour fields. */
public class ThemeEditorController {

    public interface ThemeListener {
        void onThemeSaved(ThemeDefinition theme);
    }

    private final Context context;
    private final ThemeListener listener;

    public ThemeEditorController(Context context, ThemeListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show(ThemeDefinition theme) {
        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * context.getResources()
                .getDisplayMetrics().density);
        root.setPadding(padding, padding, padding, padding);
        EditText name = field(root, "Name", theme.getName());
        CheckBox dark = new CheckBox(context);
        dark.setText(R.string.library_theme_dark);
        dark.setChecked(theme.isDarkMode());
        root.addView(dark);
        EditText primary = field(root, "Primary", theme.getPrimaryColor());
        EditText primaryDark = field(root, "Primary dark",
                theme.getPrimaryDarkColor());
        EditText accent = field(root, "Accent", theme.getAccentColor());
        EditText background = field(root, "Background",
                theme.getBackgroundColor());
        EditText surface = field(root, "Surface", theme.getSurfaceColor());
        EditText text = field(root, "Text", theme.getTextColor());
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.library_theme_edit_title)
                .setView(root)
                .setPositiveButton(R.string.library_save, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(d -> {
            Button save = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            save.setOnClickListener(v -> {
                theme.setName(name.getText().toString().trim());
                theme.setDarkMode(dark.isChecked());
                theme.setPrimaryColor(primary.getText().toString().trim());
                theme.setPrimaryDarkColor(primaryDark.getText().toString().trim());
                theme.setAccentColor(accent.getText().toString().trim());
                theme.setBackgroundColor(background.getText().toString().trim());
                theme.setSurfaceColor(surface.getText().toString().trim());
                theme.setTextColor(text.getText().toString().trim());
                List<String> problems = ThemeValidator.validate(theme);
                if (!problems.isEmpty()) {
                    Toast.makeText(context, problems.get(0),
                            Toast.LENGTH_LONG).show();
                    return;
                }
                listener.onThemeSaved(theme);
                dialog.dismiss();
            });
        });
        dialog.show();
    }

    private EditText field(LinearLayout root, String label, String value) {
        TextView caption = new TextView(context);
        caption.setText(label);
        root.addView(caption);
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(value == null ? "" : value);
        root.addView(input);
        return input;
    }
}
