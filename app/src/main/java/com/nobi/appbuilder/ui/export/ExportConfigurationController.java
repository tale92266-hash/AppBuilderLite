package com.nobi.appbuilder.ui.export;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.export.ProjectPackagingConfig;

import java.util.List;

/** Binds the export configuration form to a ProjectPackagingConfig. */
public class ExportConfigurationController {

    public interface OutputDirectoryListener {
        void onChangeOutputDirectory();
    }

    private final View root;
    private final EditText projectName;
    private final EditText appName;
    private final EditText packageName;
    private final EditText minSdk;
    private final EditText targetSdk;
    private final EditText versionName;
    private final EditText versionCode;
    private final EditText description;
    private final TextView outputDir;
    private final CheckBox includeWrapper;

    public ExportConfigurationController(View root,
                                         OutputDirectoryListener listener) {
        this.root = root;
        projectName = root.findViewById(R.id.input_export_project_name);
        appName = root.findViewById(R.id.input_export_app_name);
        packageName = root.findViewById(R.id.input_export_package);
        minSdk = root.findViewById(R.id.input_export_min_sdk);
        targetSdk = root.findViewById(R.id.input_export_target_sdk);
        versionName = root.findViewById(R.id.input_export_version_name);
        versionCode = root.findViewById(R.id.input_export_version_code);
        description = root.findViewById(R.id.input_export_description);
        outputDir = root.findViewById(R.id.text_export_output_dir);
        includeWrapper = root.findViewById(R.id.check_export_wrapper);
        root.findViewById(R.id.btn_export_change_dir)
                .setOnClickListener(v -> listener.onChangeOutputDirectory());
    }

    public void bind(ProjectPackagingConfig config, String outputDirLabel) {
        projectName.setText(config.getProjectName());
        appName.setText(config.getAppName());
        packageName.setText(config.getPackageName());
        minSdk.setText(String.valueOf(config.getMinSdk()));
        targetSdk.setText(String.valueOf(config.getTargetSdk()));
        versionName.setText(config.getVersionName());
        versionCode.setText(String.valueOf(config.getVersionCode()));
        description.setText(config.getDescription() == null ? ""
                : config.getDescription());
        includeWrapper.setChecked(config.isIncludeWrapper());
        outputDir.setText(outputDirLabel);
    }

    public void setOutputDirLabel(String label) {
        outputDir.setText(label);
    }

    /** Reads the form; returns null and fills errors when invalid. */
    public ProjectPackagingConfig readConfig(ProjectPackagingConfig base,
                                             List<String> errors) {
        ProjectPackagingConfig config = new ProjectPackagingConfig();
        config.setProjectName(textOf(projectName));
        config.setAppName(textOf(appName));
        config.setPackageName(textOf(packageName));
        config.setMinSdk(parseInt(textOf(minSdk), base.getMinSdk()));
        config.setTargetSdk(parseInt(textOf(targetSdk), base.getTargetSdk()));
        config.setVersionName(textOf(versionName));
        config.setVersionCode(parseInt(textOf(versionCode),
                base.getVersionCode()));
        config.setDescription(textOf(description));
        config.setIncludeWrapper(includeWrapper.isChecked());
        config.setOutputTreeUri(base.getOutputTreeUri());
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
