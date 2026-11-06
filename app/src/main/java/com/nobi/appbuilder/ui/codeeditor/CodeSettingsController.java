package com.nobi.appbuilder.ui.codeeditor;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.codeeditor.EditorSettings;

/** Editor settings dialog, persisted via EditorSettings. */
public class CodeSettingsController {

    public interface SettingsListener {
        void onSettingsChanged();
    }

    private final Context context;
    private final EditorSettings settings;
    private final SettingsListener listener;

    public CodeSettingsController(Context context, EditorSettings settings,
                                  SettingsListener listener) {
        this.context = context;
        this.settings = settings;
        this.listener = listener;
    }

    public void showDialog() {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_code_settings, null);
        Spinner themeSpinner = view.findViewById(R.id.spinner_code_theme);
        themeSpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Light", "Dark"}));
        themeSpinner.setSelection(settings.getTheme());
        SeekBar fontSize = view.findViewById(R.id.seek_code_font_size);
        TextView fontSizeLabel =
                view.findViewById(R.id.text_code_font_size);
        fontSize.setMax(12);
        fontSize.setProgress((int) settings.getFontSizeSp() - 10);
        fontSizeLabel.setText((int) settings.getFontSizeSp() + " sp");
        fontSize.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar,
                                                  int progress,
                                                  boolean fromUser) {
                        fontSizeLabel.setText((progress + 10) + " sp");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });
        CheckBox lineNumbers =
                view.findViewById(R.id.check_code_line_numbers);
        lineNumbers.setChecked(settings.isLineNumbers());
        CheckBox currentLine =
                view.findViewById(R.id.check_code_current_line);
        currentLine.setChecked(settings.isCurrentLineHighlight());
        CheckBox wordWrap = view.findViewById(R.id.check_code_word_wrap);
        wordWrap.setChecked(settings.isWordWrap());
        CheckBox autoIndent = view.findViewById(R.id.check_code_auto_indent);
        autoIndent.setChecked(settings.isAutoIndent());
        CheckBox highlighting =
                view.findViewById(R.id.check_code_highlighting);
        highlighting.setChecked(settings.isSyntaxHighlighting());
        CheckBox completion =
                view.findViewById(R.id.check_code_completion);
        completion.setChecked(settings.isAutoCompletion());
        CheckBox bracketMatching =
                view.findViewById(R.id.check_code_bracket_matching);
        bracketMatching.setChecked(settings.isBracketMatching());
        CheckBox autoSave = view.findViewById(R.id.check_code_auto_save);
        autoSave.setChecked(settings.isAutoSave());
        new AlertDialog.Builder(context)
                .setTitle(R.string.editor_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    settings.setTheme(themeSpinner.getSelectedItemPosition());
                    settings.setFontSizeSp(fontSize.getProgress() + 10);
                    settings.setLineNumbers(lineNumbers.isChecked());
                    settings.setCurrentLineHighlight(currentLine.isChecked());
                    settings.setWordWrap(wordWrap.isChecked());
                    settings.setAutoIndent(autoIndent.isChecked());
                    settings.setSyntaxHighlighting(highlighting.isChecked());
                    settings.setAutoCompletion(completion.isChecked());
                    settings.setBracketMatching(bracketMatching.isChecked());
                    settings.setAutoSave(autoSave.isChecked());
                    listener.onSettingsChanged();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
