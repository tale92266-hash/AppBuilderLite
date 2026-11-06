package com.nobi.appbuilder;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.nobi.appbuilder.ui.dashboard.DashboardActivity;

/**
 * Launcher entry point. Shows a short splash, applies the saved theme,
 * then hands over to the dashboard. All app UI text is English;
 * the dashboard is the real home screen.
 */
public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applySavedTheme();
        setContentView(R.layout.activity_main);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
        }, SPLASH_DELAY_MS);
    }

    private void applySavedTheme() {
        boolean dark = getSharedPreferences("appbuilder_prefs", MODE_PRIVATE)
                .getBoolean("dark_theme", false);
        AppCompatDelegate.setDefaultNightMode(dark
                ? AppCompatDelegate.MODE_NIGHT_YES
                : AppCompatDelegate.MODE_NIGHT_NO);
    }
}
