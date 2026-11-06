package com.nobi.appbuilder.ui.build;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.build.BuildConfiguration;

import java.util.List;

/** Binds the build configuration form to a BuildConfiguration. */
public class BuildConfigurationController {

    /** Called when the user picks another build mode. */
    public interface ModeChangeListener {
        void onModeChanged(String buildMode);
    }

    private final View root;
    private final Spinner buildMode;
    private final TextView cloudStatus;
    private final Spinner buildType;
    private final EditText appId;
    private final EditText versionName;
    private final EditText versionCode;
    private final EditText minSdk;
    private final EditText targetSdk;
    private final EditText sdkPath;
    private final CheckBox shrink;
    private final CheckBox resourceShrink;
    private final CheckBox includeReport;
    private final CheckBox preserveLogs;

    public BuildConfigurationController(View root) {
        this.root = root;
        buildMode = root.findViewById(R.id.spinner_build_mode);
        cloudStatus = root.findViewById(R.id.text_build_cloud_status);
        buildType = root.findViewById(R.id.spinner_build_type);
        appId = root.findViewById(R.id.input_build_app_id);
        versionName = root.findViewById(R.id.input_build_version_name);
        versionCode = root.findViewById(R.id.input_build_version_code);
        minSdk = root.findViewById(R.id.input_build_min_sdk);
        targetSdk = root.findViewById(R.id.input_build_target_sdk);
        sdkPath = root.findViewById(R.id.input_build_sdk_path);
        shrink = root.findViewById(R.id.check_build_shrink);
        resourceShrink = root.findViewById(R.id.check_build_resource_shrink);
        includeReport = root.findViewById(R.id.check_build_report);
        preserveLogs = root.findViewById(R.id.check_build_logs);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                root.getContext(), R.array.build_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        buildType.setAdapter(adapter);
        ArrayAdapter<CharSequence> modeAdapter =
                ArrayAdapter.createFromResource(root.getContext(),
                        R.array.build_modes,
                        android.R.layout.simple_spinner_item);
        modeAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        buildMode.setAdapter(modeAdapter);
    }

    public void setModeChangeListener(ModeChangeListener listener) {
        buildMode.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position,
                                               long id) {
                        if (listener != null) {
                            listener.onModeChanged(getSelectedBuildMode());
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
    }

    public String getSelectedBuildMode() {
        return buildMode.getSelectedItemPosition() == 1
                ? BuildConfiguration.MODE_EXPORT_ZIP
                : BuildConfiguration.MODE_CLOUD_APK;
    }

    public void setCloudStatusText(String text) {
        if (text == null || text.isEmpty()) {
            cloudStatus.setVisibility(View.GONE);
        } else {
            cloudStatus.setVisibility(View.VISIBLE);
            cloudStatus.setText(text);
        }
    }

    public void bind(BuildConfiguration config) {
        buildMode.setSelection(config.isCloudBuild() ? 0 : 1);
        buildType.setSelection(config.isRelease() ? 1 : 0);
        appId.setText(config.getApplicationId());
        versionName.setText(config.getVersionName());
        versionCode.setText(String.valueOf(config.getVersionCode()));
        minSdk.setText(String.valueOf(config.getMinSdk()));
        targetSdk.setText(String.valueOf(config.getTargetSdk()));
        sdkPath.setText(config.getSdkPath() == null ? ""
                : config.getSdkPath());
        shrink.setChecked(config.isShrinkEnabled());
        resourceShrink.setChecked(config.isResourceShrinkEnabled());
        includeReport.setChecked(config.isIncludeReport());
        preserveLogs.setChecked(config.isPreserveLogs());
    }

    public BuildConfiguration readConfig(BuildConfiguration base,
                                         List<String> errors) {
        BuildConfiguration config = new BuildConfiguration();
        config.setBuildMode(getSelectedBuildMode());
        config.setBuildType(buildType.getSelectedItemPosition() == 1
                ? BuildConfiguration.TYPE_RELEASE
                : BuildConfiguration.TYPE_DEBUG);
        config.setApplicationId(textOf(appId));
        config.setVersionName(textOf(versionName));
        config.setVersionCode(parseInt(textOf(versionCode),
                base.getVersionCode()));
        config.setMinSdk(parseInt(textOf(minSdk), base.getMinSdk()));
        config.setTargetSdk(parseInt(textOf(targetSdk), base.getTargetSdk()));
        String sdk = textOf(sdkPath);
        config.setSdkPath(sdk.isEmpty() ? null : sdk);
        config.setShrinkEnabled(shrink.isChecked());
        config.setResourceShrinkEnabled(resourceShrink.isChecked());
        config.setIncludeReport(includeReport.isChecked());
        config.setPreserveLogs(preserveLogs.isChecked());
        errors.addAll(config.validate());
        return errors.isEmpty() ? config : null;
    }

    private String textOf(EditText field) {
        return field.getText() == null ? "" : field.getText().toString().trim();
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return fallback;
        }
    }
}
