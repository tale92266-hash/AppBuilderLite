package com.nobi.appbuilder.ui.export;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.export.PackagingResult;

/** Success / error result views with their actions. */
public class ExportResultController {

    public interface ResultListener {
        void onOpenLocation();

        void onShare();

        void onExportAnother();

        void onViewReport();
    }

    private final View successRoot;
    private final View errorRoot;
    private final TextView successSummary;
    private final TextView errorText;

    public ExportResultController(View root, ResultListener listener) {
        successRoot = root.findViewById(R.id.layout_export_success);
        errorRoot = root.findViewById(R.id.layout_export_error);
        successSummary = root.findViewById(R.id.text_export_success_summary);
        errorText = root.findViewById(R.id.text_export_error);
        Button open = root.findViewById(R.id.btn_export_open_location);
        Button share = root.findViewById(R.id.btn_export_share);
        Button another = root.findViewById(R.id.btn_export_another);
        Button report = root.findViewById(R.id.btn_export_view_report);
        Button retry = root.findViewById(R.id.btn_export_retry);
        open.setOnClickListener(v -> listener.onOpenLocation());
        share.setOnClickListener(v -> listener.onShare());
        another.setOnClickListener(v -> listener.onExportAnother());
        report.setOnClickListener(v -> listener.onViewReport());
        retry.setOnClickListener(v -> listener.onExportAnother());
    }

    public void showSuccess(PackagingResult result) {
        errorRoot.setVisibility(View.GONE);
        successRoot.setVisibility(View.VISIBLE);
        StringBuilder out = new StringBuilder();
        out.append(result.getConfig().getProjectName()).append("\n");
        out.append(result.getReport().getZipFileName()).append(" · ")
                .append(formatSize(result.getReport().getZipSizeBytes()))
                .append("\n");
        out.append(result.getReport().getJavaFiles()).append(" Java · ")
                .append(result.getReport().getXmlFiles()).append(" XML · ")
                .append(result.getReport().getResources())
                .append(" resources · ")
                .append(result.getReport().getActivities())
                .append(" activities\n");
        out.append("Location: ");
        if (result.getZipFile() != null) {
            out.append(result.getZipFile().getAbsolutePath());
        } else if (result.getOutputUri() != null) {
            out.append(result.getOutputUri());
        }
        successSummary.setText(out.toString());
    }

    public void showError(String message) {
        successRoot.setVisibility(View.GONE);
        errorRoot.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }

    public void hide() {
        successRoot.setVisibility(View.GONE);
        errorRoot.setVisibility(View.GONE);
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
