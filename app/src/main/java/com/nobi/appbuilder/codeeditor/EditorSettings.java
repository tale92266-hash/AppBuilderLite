package com.nobi.appbuilder.codeeditor;

import android.content.Context;
import android.content.SharedPreferences;

/** Persisted editor preferences (SharedPreferences appbuilder_prefs
 * pattern — never hardcoded through the editor). */
public class EditorSettings {

    public static final String PREFS = "code_editor_prefs";
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;

    private final SharedPreferences prefs;

    public EditorSettings(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public int getTheme() {
        return prefs.getInt("theme", THEME_LIGHT);
    }

    public void setTheme(int theme) {
        prefs.edit().putInt("theme", theme).apply();
    }

    public float getFontSizeSp() {
        return prefs.getFloat("font_size_sp", 14f);
    }

    public void setFontSizeSp(float size) {
        prefs.edit().putFloat("font_size_sp", size).apply();
    }

    public boolean isLineNumbers() {
        return prefs.getBoolean("line_numbers", true);
    }

    public void setLineNumbers(boolean enabled) {
        prefs.edit().putBoolean("line_numbers", enabled).apply();
    }

    public boolean isCurrentLineHighlight() {
        return prefs.getBoolean("current_line_highlight", true);
    }

    public void setCurrentLineHighlight(boolean enabled) {
        prefs.edit().putBoolean("current_line_highlight", enabled).apply();
    }

    public boolean isWordWrap() {
        return prefs.getBoolean("word_wrap", false);
    }

    public void setWordWrap(boolean enabled) {
        prefs.edit().putBoolean("word_wrap", enabled).apply();
    }

    public int getTabWidth() {
        return prefs.getInt("tab_width", 4);
    }

    public void setTabWidth(int width) {
        prefs.edit().putInt("tab_width", Math.max(2, Math.min(8, width)))
                .apply();
    }

    public boolean isAutoIndent() {
        return prefs.getBoolean("auto_indent", true);
    }

    public void setAutoIndent(boolean enabled) {
        prefs.edit().putBoolean("auto_indent", enabled).apply();
    }

    public boolean isSyntaxHighlighting() {
        return prefs.getBoolean("syntax_highlighting", true);
    }

    public void setSyntaxHighlighting(boolean enabled) {
        prefs.edit().putBoolean("syntax_highlighting", enabled).apply();
    }

    public boolean isAutoCompletion() {
        return prefs.getBoolean("auto_completion", true);
    }

    public void setAutoCompletion(boolean enabled) {
        prefs.edit().putBoolean("auto_completion", enabled).apply();
    }

    public boolean isBracketMatching() {
        return prefs.getBoolean("bracket_matching", true);
    }

    public void setBracketMatching(boolean enabled) {
        prefs.edit().putBoolean("bracket_matching", enabled).apply();
    }

    public boolean isAutoSave() {
        return prefs.getBoolean("auto_save", true);
    }

    public void setAutoSave(boolean enabled) {
        prefs.edit().putBoolean("auto_save", enabled).apply();
    }
}
