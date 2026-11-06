package com.nobi.appbuilder.ui.sync;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.sync.conflict.ConflictRecord;
import com.nobi.appbuilder.sync.conflict.ConflictResolution;

import java.util.ArrayList;
import java.util.List;

/**
 * The professional conflict dialog: five explicit options, never a
 * silent overwrite. REVIEW opens the change preview dialog.
 */
public class ConflictDialogController {

    public interface ConflictListener {
        void onResolutionChosen(ConflictRecord record,
                                ConflictResolution resolution);
    }

    private final Context context;
    private final ConflictListener listener;

    public ConflictDialogController(Context context,
                                    ConflictListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show(ConflictRecord record) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_sync_conflict, null);
        TextView title = view.findViewById(R.id.text_sync_conflict_title);
        title.setText("Sync conflict — " + record.getScreenId());
        TextView message =
                view.findViewById(R.id.text_sync_conflict_message);
        message.setText("The visual editor and the XML file disagree on "
                + (record.getComponentId() == null ? "this screen"
                : "component " + record.getComponentId())
                + ". Choose which version to keep — nothing is "
                + "overwritten until you decide.");
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();
        view.findViewById(R.id.btn_sync_keep_visual).setOnClickListener(
                v -> {
                    dialog.dismiss();
                    listener.onResolutionChosen(record,
                            ConflictResolution.KEEP_VISUAL);
                });
        view.findViewById(R.id.btn_sync_keep_code).setOnClickListener(
                v -> {
                    dialog.dismiss();
                    listener.onResolutionChosen(record,
                            ConflictResolution.KEEP_CODE);
                });
        view.findViewById(R.id.btn_sync_review).setOnClickListener(
                v -> {
                    dialog.dismiss();
                    listener.onResolutionChosen(record,
                            ConflictResolution.REVIEW);
                });
        view.findViewById(R.id.btn_sync_backup).setOnClickListener(
                v -> {
                    dialog.dismiss();
                    listener.onResolutionChosen(record,
                            ConflictResolution.BACKUP_MANUAL);
                });
        view.findViewById(R.id.btn_sync_cancel).setOnClickListener(
                v -> {
                    dialog.dismiss();
                    listener.onResolutionChosen(record,
                            ConflictResolution.CANCEL);
                });
        dialog.show();
    }

    /** Shows a proposed-change preview (the REVIEW option). */
    public void showPreview(List<String> changes) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_sync_preview, null);
        RecyclerView recycler =
                view.findViewById(R.id.recycler_sync_preview);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setAdapter(new PreviewAdapter(changes));
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.sync_preview_title)
                .setView(view)
                .create();
        view.findViewById(R.id.btn_sync_preview_close).setOnClickListener(
                v -> dialog.dismiss());
        dialog.show();
    }

    private static class PreviewAdapter
            extends RecyclerView.Adapter<PreviewAdapter.Holder> {

        private final List<String> changes;

        PreviewAdapter(List<String> changes) {
            this.changes = changes == null ? new ArrayList<>() : changes;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sync_change, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            holder.title.setText(changes.get(position));
            holder.detail.setText("");
        }

        @Override
        public int getItemCount() {
            return changes.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView detail;

            Holder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.text_sync_change_title);
                detail =
                        itemView.findViewById(R.id.text_sync_change_detail);
            }
        }
    }
}
