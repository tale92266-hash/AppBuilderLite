package com.nobi.appbuilder.ui.preview;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.nobi.appbuilder.R;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.preview.PreviewController;
import com.nobi.appbuilder.preview.PreviewSession;
import com.nobi.appbuilder.preview.PreviewState;
import com.nobi.appbuilder.preview.device.DeviceProfileManager;
import com.nobi.appbuilder.preview.device.PreviewDeviceProfile;
import com.nobi.appbuilder.preview.device.PreviewViewport;
import com.nobi.appbuilder.preview.theme.PreviewThemeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Live native preview: the current screen rendered as real Android
 * Views inside an optional device frame. Top toolbar (back, screen
 * name, restart, theme, close), bottom toolbar (device profile,
 * orientation, zoom). Closing returns to the editor — the editor
 * activity was never finished, so its selection, zoom and editing
 * state are exactly where the user left them.
 */
public class PreviewActivity extends AppCompatActivity {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_SCREEN_ID = "screen_id";

    private static final String STATE_SCREEN_ID = "screen_id";
    private static final String STATE_HISTORY = "history";
    private static final String STATE_THEME_MODE = "theme_mode";
    private static final String STATE_PROFILE_NAME = "profile_name";
    private static final String STATE_PROFILE_W = "profile_w";
    private static final String STATE_PROFILE_H = "profile_h";
    private static final String STATE_PROFILE_CUSTOM = "profile_custom";
    private static final String STATE_ORIENTATION = "orientation";
    private static final String STATE_ZOOM = "zoom";
    private static final String STATE_FRAME = "frame";

    private DatabaseHelper dbHelper;
    private PreviewSession session;
    private PreviewState previewState;
    private PreviewController controller;
    private PreviewThemeManager themeManager;

    private DeviceFrameView deviceFrame;
    private Button buttonDevice;
    private Button buttonOrientation;
    private TextView textZoom;
    private boolean errorsShownOnce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Intent intent = getIntent();
        String projectId = intent.getStringExtra(EXTRA_PROJECT_ID);
        String screenId = intent.getStringExtra(EXTRA_SCREEN_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.preview_load_failed,
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);
        DeviceProfileManager profiles = new DeviceProfileManager();
        PreviewDeviceProfile profile = profiles.getDefault();
        int themeMode = PreviewThemeManager.MODE_CUSTOM;
        String orientation = PreviewSession.ORIENTATION_PORTRAIT;
        float zoom = 1.0f;
        boolean frameVisible = true;
        ArrayList<String> history = null;
        if (savedInstanceState != null) {
            screenId = savedInstanceState.getString(STATE_SCREEN_ID, screenId);
            themeMode = savedInstanceState.getInt(STATE_THEME_MODE,
                    PreviewThemeManager.MODE_CUSTOM);
            orientation = savedInstanceState.getString(STATE_ORIENTATION,
                    PreviewSession.ORIENTATION_PORTRAIT);
            zoom = savedInstanceState.getFloat(STATE_ZOOM, 1.0f);
            frameVisible = savedInstanceState.getBoolean(STATE_FRAME, true);
            history = savedInstanceState.getStringArrayList(STATE_HISTORY);
            if (savedInstanceState.getBoolean(STATE_PROFILE_CUSTOM, false)) {
                profile = profiles.createCustom(
                        savedInstanceState.getInt(STATE_PROFILE_W, 393),
                        savedInstanceState.getInt(STATE_PROFILE_H, 873));
            } else {
                profile = profiles.getByName(
                        savedInstanceState.getString(STATE_PROFILE_NAME, null));
            }
        }

        session = new PreviewSession(projectId, screenId, profile);
        session.setThemeMode(themeMode);
        session.setOrientation(orientation);
        if (history != null) {
            session.restoreHistory(history);
        }
        previewState = new PreviewState();
        previewState.setZoom(zoom);
        previewState.setFrameVisible(frameVisible);

        themeManager = new PreviewThemeManager(null);
        themeManager.setMode(themeMode);
        controller = new PreviewController(this, dbHelper, session, themeManager);
        controller.setActivity(this);
        controller.setRenderListener(new PreviewController.RenderListener() {
            @Override
            public void onRendered(View rootView, List<String> errors,
                                   ScreenModel screen) {
                onPreviewRendered(rootView, errors, screen);
            }

            @Override
            public void onLoadFailed(String message) {
                Toast.makeText(PreviewActivity.this, message,
                        Toast.LENGTH_LONG).show();
            }
        });

        MaterialToolbar toolbar = findViewById(R.id.preview_toolbar);
        PreviewToolbarController toolbarController =
                new PreviewToolbarController(toolbar,
                        new PreviewToolbarController.ToolbarListener() {
                            @Override
                            public void onBack() {
                                handleBack();
                            }

                            @Override
                            public void onRestart() {
                                controller.restart();
                            }

                            @Override
                            public void onTheme() {
                                showThemeMenu(toolbar);
                            }

                            @Override
                            public void onClose() {
                                finish();
                            }
                        });
        toolbarController.setTitle(getString(R.string.preview_loading));

        deviceFrame = findViewById(R.id.device_frame);
        FrameLayout stage = findViewById(R.id.preview_stage);
        TextView loading = new TextView(this);
        loading.setText(R.string.preview_loading);
        loading.setPadding(32, 32, 32, 32);
        stage.addView(loading);

        buttonDevice = findViewById(R.id.btn_device_profile);
        buttonOrientation = findViewById(R.id.btn_orientation);
        ImageButton zoomOut = findViewById(R.id.btn_zoom_out);
        ImageButton zoomIn = findViewById(R.id.btn_zoom_in);
        textZoom = findViewById(R.id.text_zoom);

        buttonDevice.setOnClickListener(v -> DeviceSelectorDialog.show(
                PreviewActivity.this, session.getDeviceProfile(),
                previewState.isFrameVisible(),
                (chosen, showFrame) -> {
                    session.setDeviceProfile(chosen);
                    previewState.setFrameVisible(showFrame);
                    applyFrameSize();
                    updateBottomBar();
                }));
        buttonOrientation.setOnClickListener(v -> {
            session.toggleOrientation();
            applyFrameSize();
            updateBottomBar();
        });
        zoomOut.setOnClickListener(v -> {
            previewState.zoomOut();
            applyFrameSize();
            updateBottomBar();
        });
        zoomIn.setOnClickListener(v -> {
            previewState.zoomIn();
            applyFrameSize();
            updateBottomBar();
        });
        updateBottomBar();

        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        handleBack();
                    }
                });

        controller.loadInitial();
    }

    private void onPreviewRendered(View rootView, List<String> errors,
                                   ScreenModel screen) {
        // The loading label is a sibling of the frame; drop it on
        // first render, then show the rendered root in the frame.
        FrameLayout stage = findViewById(R.id.preview_stage);
        for (int i = stage.getChildCount() - 1; i >= 0; i--) {
            View child = stage.getChildAt(i);
            if (child != deviceFrame) {
                stage.removeViewAt(i);
            }
        }
        deviceFrame.setContent(rootView);
        applyFrameSize();
        if (screen != null) {
            MaterialToolbar toolbar = findViewById(R.id.preview_toolbar);
            toolbar.setTitle(screen.getName());
        }
        if (errors != null && !errors.isEmpty()) {
            if (!errorsShownOnce) {
                errorsShownOnce = true;
                StringBuilder message = new StringBuilder();
                for (String error : errors) {
                    message.append("• ").append(error).append("\n");
                }
                new AlertDialog.Builder(this)
                        .setTitle(R.string.preview_errors_title)
                        .setMessage(message.toString().trim())
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            } else {
                Toast.makeText(this,
                        getString(R.string.preview_issues, errors.size()),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showThemeMenu(View anchor) {
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, PreviewThemeManager.MODE_LIGHT, 0,
                R.string.theme_light);
        menu.getMenu().add(0, PreviewThemeManager.MODE_DARK, 0,
                R.string.theme_dark);
        menu.getMenu().add(0, PreviewThemeManager.MODE_CUSTOM, 0,
                R.string.theme_project);
        menu.setOnMenuItemClickListener(item -> {
            controller.setThemeMode(item.getItemId());
            return true;
        });
        menu.show();
    }

    private void applyFrameSize() {
        if (deviceFrame == null || session == null) {
            return;
        }
        int width = PreviewViewport.frameWidthPx(this,
                session.getDeviceProfile(), session.getOrientation(),
                previewState.getZoom());
        int height = PreviewViewport.frameHeightPx(this,
                session.getDeviceProfile(), session.getOrientation(),
                previewState.getZoom());
        deviceFrame.applySize(width, height, previewState.isFrameVisible());
    }

    private void updateBottomBar() {
        if (buttonDevice != null && session != null) {
            buttonDevice.setText(session.getDeviceProfile().getName());
        }
        if (buttonOrientation != null && session != null) {
            buttonOrientation.setText(session.isLandscape()
                    ? R.string.preview_orientation_landscape
                    : R.string.preview_orientation_portrait);
        }
        if (textZoom != null && previewState != null) {
            textZoom.setText(getString(R.string.preview_zoom,
                    previewState.getZoomPercent()));
        }
    }

    private void handleBack() {
        if (controller != null && controller.getNavigator() != null
                && controller.getNavigator().canGoBack()) {
            controller.goBack();
        } else {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (session == null) {
            return;
        }
        outState.putString(STATE_SCREEN_ID, session.getCurrentScreenId());
        outState.putStringArrayList(STATE_HISTORY,
                new ArrayList<>(session.getHistorySnapshot()));
        outState.putInt(STATE_THEME_MODE, session.getThemeMode());
        PreviewDeviceProfile profile = session.getDeviceProfile();
        outState.putString(STATE_PROFILE_NAME, profile.getName());
        outState.putInt(STATE_PROFILE_W, profile.getWidthDp());
        outState.putInt(STATE_PROFILE_H, profile.getHeightDp());
        outState.putBoolean(STATE_PROFILE_CUSTOM, profile.isCustom());
        outState.putString(STATE_ORIENTATION, session.getOrientation());
        outState.putFloat(STATE_ZOOM, previewState.getZoom());
        outState.putBoolean(STATE_FRAME, previewState.isFrameVisible());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Phase 11: live reload — a sync change re-renders the
        // current screen without restarting the preview.
        com.nobi.appbuilder.sync.reload.PreviewRefreshManager.register(
                request -> {
                    if (controller != null) {
                        controller.renderCurrent();
                    }
                });
    }

    @Override
    protected void onPause() {
        com.nobi.appbuilder.sync.reload.PreviewRefreshManager.unregister();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (controller != null) {
            controller.release();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
