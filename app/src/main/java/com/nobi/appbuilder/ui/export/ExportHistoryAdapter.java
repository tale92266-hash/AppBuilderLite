package com.nobi.appbuilder.ui.export;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.export.storage.ExportHistoryRepository.ExportRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Previous exports: name, date, size, status. */
public class ExportHistoryAdapter
        extends RecyclerView.Adapter<ExportHistoryAdapter.Holder> {

    public interface OnHistoryActionListener {
        void onOpen(ExportRecord record);

        void onShare(ExportRecord record);

        void onDelete(ExportRecord record);
    }

    private final List<ExportRecord> records = new ArrayList<>();
    private final OnHistoryActionListener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public ExportHistoryAdapter(OnHistoryActionListener listener) {
        this.listener = listener;
    }

    public void setRecords(List<ExportRecord> records) {
        this.records.clear();
        this.records.addAll(records);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_export_history, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ExportRecord record = records.get(position);
        holder.name.setText(record.projectName);
        holder.detail.setText(record.zipFileName + " · "
                + formatSize(record.fileSize) + " · "
                + dateFormat.format(new Date(record.exportDate)));
        holder.status.setText(record.status);
        holder.status.setTextColor("success".equals(record.status)
                ? 0xFF2E7D32 : 0xFFB3261E);
        holder.itemView.setOnClickListener(v -> listener.onOpen(record));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(record);
            return true;
        });
        holder.share.setOnClickListener(v -> listener.onShare(record));
    }

    @Override
    public int getItemCount() {
        return records.size();
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

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView detail;
        final TextView status;
        final View share;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_export_history_name);
            detail = itemView.findViewById(R.id.text_export_history_detail);
            status = itemView.findViewById(R.id.text_export_history_status);
            share = itemView.findViewById(R.id.btn_export_history_share);
        }
    }
}
