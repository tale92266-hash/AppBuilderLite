package com.nobi.appbuilder.ui.build;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.build.BuildResult;

/** Success / failure result views with APK actions. */
public class BuildResultController {

    public interface ResultListener {
        void onInstallApk();

        void onShareApk();

        void onOpenApkLocation();

        void onDeleteApk();

        void onViewReport();

        void onBuildAnother();
    }

    private final View successRoot;
    private final View failedRoot;
    private final View apkActionsRow;
    private final View apkLocationRow;
    private final TextView successSummary;
    private final TextView failedText;

    public BuildResultController(View root, ResultListener listener) {
        successRoot = root.findViewById(R.id.layout_build_success);
        failedRoot = root.findViewById(R.id.layout_build_failed);
        apkActionsRow = root.findViewById(R.id.row_build_apk_actions);
        apkLocationRow = root.findViewById(R.id.row_build_apk_location);
        successSummary = root.findViewById(R.id.text_build_success_summary);
        failedText = root.findViewById(R.id.text_build_failed);
        Button install = root.findViewById(R.id.btn_build_install);
        Button share = root.findViewById(R.id.btn_build_share_apk);
        Button open = root.findViewById(R.id.btn_build_open_location);
        Button delete = root.findViewById(R.id.btn_build_delete_apk);
        Button report = root.findViewById(R.id.btn_build_view_report);
        Button another = root.findViewById(R.id.btn_build_another);
        Button retry = root.findViewById(R.id.btn_build_retry);
        install.setOnClickListener(v -> listener.onInstallApk());
        share.setOnClickListener(v -> listener.onShareApk());
        open.setOnClickListener(v -> listener.onOpenApkLocation());
        delete.setOnClickListener(v -> listener.onDeleteApk());
        report.setOnClickListener(v -> listener.onViewReport());
        another.setOnClickListener(v -> listener.onBuildAnother());
        retry.setOnClickListener(v -> listener.onBuildAnother());
    }

    public void showSuccess(BuildResult result) {
        failedRoot.setVisibility(View.GONE);
        successRoot.setVisibility(View.VISIBLE);
        apkActionsRow.setVisibility(View.VISIBLE);
        apkLocationRow.setVisibility(View.VISIBLE);
        StringBuilder out = new StringBuilder();
        out.append("Build succeeded in ").append(result.getDurationMs())
                .append(" ms\n");
        if (result.getApkFile() != null) {
            out.append(result.getApkFile().getName()).append(" · ")
                    .append(formatSize(result.getApkFile().length()))
                    .append("\n");
            out.append(result.getApkPackageName()).append(" · ")
                    .append(result.getApkVersionName()).append(" (")
                    .append(result.getApkVersionCode()).append(")\n");
            out.append(result.getApkFile().getAbsolutePath());
        }
        successSummary.setText(out.toString());
    }

    public void showExternal(BuildResult result) {
        failedRoot.setVisibility(View.GONE);
        successRoot.setVisibility(View.VISIBLE);
        // An external build produces a ZIP, not an APK — the Install,
        // Share, Open location and Delete buttons must not be shown:
        // with no APK file their taps were silent no-ops.
        apkActionsRow.setVisibility(View.GONE);
        apkLocationRow.setVisibility(View.GONE);
        StringBuilder out = new StringBuilder();
        out.append("This device cannot compile Android apps locally.\n");
        out.append("The Android Studio project was exported:\n");
        out.append(result.getExternalZipPath()).append("\n");
        out.append("Open it in Android Studio and run ./gradlew ")
                .append(result.getConfiguration().getGradleTask());
        successSummary.setText(out.toString());
    }

    public void showFailed(String message) {
        successRoot.setVisibility(View.GONE);
        failedRoot.setVisibility(View.VISIBLE);
        failedText.setText(message);
    }

    public void hide() {
        successRoot.setVisibility(View.GONE);
        failedRoot.setVisibility(View.GONE);
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return (bytes / 1024) + " KB";
        }
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
