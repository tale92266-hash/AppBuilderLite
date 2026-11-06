package com.nobi.appbuilder.ui.build;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.build.BuildTask;

/** Build progress: current stage, indeterminate-or-real progress,
 * elapsed time, live error/warning counts. */
public class BuildProgressController {

    private final View root;
    private final ProgressBar progressBar;
    private final TextView stageText;
    private final TextView percentText;
    private final TextView elapsedText;
    private final TextView countsText;
    private long startedAt;

    public BuildProgressController(View root) {
        this.root = root;
        progressBar = root.findViewById(R.id.progress_build);
        stageText = root.findViewById(R.id.text_build_stage);
        percentText = root.findViewById(R.id.text_build_percent);
        elapsedText = root.findViewById(R.id.text_build_elapsed);
        countsText = root.findViewById(R.id.text_build_counts);
    }

    public void show() {
        root.setVisibility(View.VISIBLE);
        startedAt = System.currentTimeMillis();
        setTask(BuildTask.CHECK_ENVIRONMENT);
        setProgress(0, false);
        setCounts(0, 0);
    }

    public void hide() {
        root.setVisibility(View.GONE);
    }

    public void setTask(BuildTask task) {
        stageText.setText(task.getLabel());
        updateElapsed();
    }

    /** Percent is only shown when the build reports a real one;
     * otherwise the bar stays indeterminate — never invented. */
    public void setProgress(int percent, boolean determinate) {
        progressBar.setIndeterminate(!determinate);
        if (determinate) {
            progressBar.setProgress(percent);
            percentText.setText(percent + "%");
        } else {
            percentText.setText("");
        }
        updateElapsed();
    }

    public void setCounts(int errors, int warnings) {
        countsText.setText(errors + " errors · " + warnings + " warnings");
    }

    private void updateElapsed() {
        long seconds = (System.currentTimeMillis() - startedAt) / 1000;
        elapsedText.setText(seconds + "s");
    }
}
