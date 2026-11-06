package com.nobi.appbuilder.ui.sync;

import android.view.View;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.sync.SynchronizationState;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Binds layout_sync_status.xml to a SynchronizationState. */
public class SyncStatusController {

    public interface StatusListener {
        void onSyncNow();

        void onReviewChanges();

        void onOpenHistory();

        void onOpenSettings();
    }

    private final TextView statusView;
    private final TextView lastView;
    private final TextView countsView;

    public SyncStatusController(View root, StatusListener listener) {
        statusView = root.findViewById(R.id.text_sync_status);
        lastView = root.findViewById(R.id.text_sync_last);
        countsView = root.findViewById(R.id.text_sync_counts);
        root.findViewById(R.id.btn_sync_now).setOnClickListener(
                v -> listener.onSyncNow());
        root.findViewById(R.id.btn_sync_review).setOnClickListener(
                v -> listener.onReviewChanges());
        root.findViewById(R.id.btn_sync_history).setOnClickListener(
                v -> listener.onOpenHistory());
        root.findViewById(R.id.btn_sync_settings).setOnClickListener(
                v -> listener.onOpenSettings());
    }

    public void refresh(SynchronizationState state) {
        statusView.setText(state.getStatus().name()
                + (state.getMessage().isEmpty() ? ""
                : " — " + state.getMessage()));
        if (state.getLastSyncTimeMs() > 0) {
            lastView.setText("Last sync: " + new SimpleDateFormat(
                    "dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(new Date(state.getLastSyncTimeMs())));
        } else {
            lastView.setText("Not synced yet");
        }
        countsView.setText(state.getPendingChanges() + " pending · "
                + state.getConflictCount() + " conflicts");
    }
}
