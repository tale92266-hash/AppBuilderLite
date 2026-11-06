package com.nobi.appbuilder.ui.export;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.export.ProjectPackagingManager;

/** Shows the current export stage, percentage and elapsed time. */
public class ExportProgressController {

    private final View root;
    private final ProgressBar progressBar;
    private final TextView stageText;
    private final TextView percentText;
    private final TextView elapsedText;
    private long startedAt;

    public ExportProgressController(View root) {
        this.root = root;
        progressBar = root.findViewById(R.id.progress_export);
        stageText = root.findViewById(R.id.text_export_stage);
        percentText = root.findViewById(R.id.text_export_percent);
        elapsedText = root.findViewById(R.id.text_export_elapsed);
    }

    public void show() {
        root.setVisibility(View.VISIBLE);
        startedAt = System.currentTimeMillis();
        setStage(0);
        setProgress(0);
    }

    public void hide() {
        root.setVisibility(View.GONE);
    }

    public void setStage(int index) {
        String label = index >= 0
                && index < ProjectPackagingManager.STAGE_LABELS.length
                ? ProjectPackagingManager.STAGE_LABELS[index] : "";
        stageText.setText(label);
        updateElapsed();
    }

    public void setProgress(int percent) {
        progressBar.setProgress(percent);
        percentText.setText(percent + "%");
        updateElapsed();
    }

    private void updateElapsed() {
        long seconds = (System.currentTimeMillis() - startedAt) / 1000;
        elapsedText.setText(seconds + "s");
    }
}
