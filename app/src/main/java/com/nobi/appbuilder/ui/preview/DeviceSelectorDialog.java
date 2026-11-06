package com.nobi.appbuilder.ui.preview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.preview.device.DeviceProfileManager;
import com.nobi.appbuilder.preview.device.PreviewDeviceProfile;

/**
 * Device picker: the four built-in profiles, a custom width/height
 * pair, and the show-frame switch. Returns the chosen profile and
 * frame flag; nothing is applied until the user taps Apply.
 */
public class DeviceSelectorDialog {

    public interface Listener {
        void onApply(PreviewDeviceProfile profile, boolean frameVisible);
    }

    public static void show(Context context, PreviewDeviceProfile current,
                            boolean frameVisible, Listener listener) {
        DeviceProfileManager manager = new DeviceProfileManager();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_device_selector, null);
        RadioGroup group = view.findViewById(R.id.group_devices);
        RadioButton radioCompact = view.findViewById(R.id.radio_compact);
        RadioButton radioStandard = view.findViewById(R.id.radio_standard);
        RadioButton radioLarge = view.findViewById(R.id.radio_large);
        RadioButton radioTablet = view.findViewById(R.id.radio_tablet);
        RadioButton radioCustom = view.findViewById(R.id.radio_custom);
        EditText inputWidth = view.findViewById(R.id.input_custom_width);
        EditText inputHeight = view.findViewById(R.id.input_custom_height);
        CheckBox checkFrame = view.findViewById(R.id.check_show_frame);
        checkFrame.setChecked(frameVisible);

        radioCompact.setText(DeviceProfileManager.COMPACT_PHONE.getDisplayLabel());
        radioStandard.setText(DeviceProfileManager.STANDARD_PHONE.getDisplayLabel());
        radioLarge.setText(DeviceProfileManager.LARGE_PHONE.getDisplayLabel());
        radioTablet.setText(DeviceProfileManager.SMALL_TABLET.getDisplayLabel());

        if (current != null && current.isCustom()) {
            radioCustom.setChecked(true);
            inputWidth.setText(String.valueOf(current.getWidthDp()));
            inputHeight.setText(String.valueOf(current.getHeightDp()));
        } else if (current != null) {
            String name = current.getName();
            if (DeviceProfileManager.COMPACT_PHONE.getName().equals(name)) {
                radioCompact.setChecked(true);
            } else if (DeviceProfileManager.LARGE_PHONE.getName().equals(name)) {
                radioLarge.setChecked(true);
            } else if (DeviceProfileManager.SMALL_TABLET.getName().equals(name)) {
                radioTablet.setChecked(true);
            } else {
                radioStandard.setChecked(true);
            }
        } else {
            radioStandard.setChecked(true);
        }
        setCustomEnabled(radioCustom.isChecked(), inputWidth, inputHeight);
        group.setOnCheckedChangeListener((g, checkedId) -> setCustomEnabled(
                checkedId == R.id.radio_custom, inputWidth, inputHeight));

        new AlertDialog.Builder(context)
                .setTitle(R.string.preview_device)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    PreviewDeviceProfile chosen;
                    int checkedId = group.getCheckedRadioButtonId();
                    if (checkedId == R.id.radio_compact) {
                        chosen = DeviceProfileManager.COMPACT_PHONE;
                    } else if (checkedId == R.id.radio_large) {
                        chosen = DeviceProfileManager.LARGE_PHONE;
                    } else if (checkedId == R.id.radio_tablet) {
                        chosen = DeviceProfileManager.SMALL_TABLET;
                    } else if (checkedId == R.id.radio_custom) {
                        int width = parseDp(inputWidth.getText().toString(),
                                current == null ? 393 : current.getWidthDp());
                        int height = parseDp(inputHeight.getText().toString(),
                                current == null ? 873 : current.getHeightDp());
                        chosen = manager.createCustom(width, height);
                    } else {
                        chosen = DeviceProfileManager.STANDARD_PHONE;
                    }
                    listener.onApply(chosen, checkFrame.isChecked());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private static void setCustomEnabled(boolean enabled, EditText width,
                                         EditText height) {
        width.setEnabled(enabled);
        height.setEnabled(enabled);
    }

    private static int parseDp(String raw, int fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
