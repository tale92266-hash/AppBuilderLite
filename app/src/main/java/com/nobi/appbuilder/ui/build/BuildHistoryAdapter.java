package com.nobi.appbuilder.ui.build;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.build.logs.BuildLogRepository.BuildRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Previous builds: type, status, duration, error/warning counts. */
public class BuildHistoryAdapter
        extends RecyclerView.Adapter<BuildHistoryAdapter.Holder> {

    public interface OnBuildHistoryActionListener {
        void onOpen(BuildRecord record);

        void onDelete(BuildRecord record);
    }

    private final List<BuildRecord> records = new ArrayList<>();
    private final OnBuildHistoryActionListener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public BuildHistoryAdapter(OnBuildHistoryActionListener listener) {
        this.listener = listener;
    }

    public void setRecords(List<BuildRecord> records) {
        this.records.clear();
        this.records.addAll(records);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_build_history, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        BuildRecord record = records.get(position);
        holder.name.setText(record.projectName + " · " + record.buildType);
        holder.detail.setText(dateFormat.format(new Date(record.startTime))
                + " · " + (record.getDuration() / 1000) + "s · "
                + record.errorCount + " errors · "
                + record.warningCount + " warnings");
        holder.status.setText(record.status);
        holder.status.setTextColor("SUCCESS".equals(record.status)
                ? 0xFF2E7D32 : 0xFFB3261E);
        holder.itemView.setOnClickListener(v -> listener.onOpen(record));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(record);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView detail;
        final TextView status;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_build_history_name);
            detail = itemView.findViewById(R.id.text_build_history_detail);
            status = itemView.findViewById(R.id.text_build_history_status);
        }
    }
}
