package com.nobi.appbuilder.ui.build;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.build.apk.ApkRepository.ApkRecord;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** APKs produced for this project, newest first. */
public class ApkListAdapter
        extends RecyclerView.Adapter<ApkListAdapter.Holder> {

    public interface OnApkActionListener {
        void onInstall(ApkRecord record);

        void onShare(ApkRecord record);

        void onDelete(ApkRecord record);
    }

    private final List<ApkRecord> records = new ArrayList<>();
    private final OnApkActionListener listener;
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    public ApkListAdapter(OnApkActionListener listener) {
        this.listener = listener;
    }

    public void setRecords(List<ApkRecord> records) {
        this.records.clear();
        this.records.addAll(records);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_apk, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ApkRecord record = records.get(position);
        holder.name.setText(record.file.getName());
        holder.detail.setText(record.buildType + " · "
                + formatSize(record.size) + " · "
                + dateFormat.format(new Date(record.modifiedAt))
                + (record.exists ? "" : " · file missing"));
        holder.itemView.setOnClickListener(v -> listener.onInstall(record));
        holder.share.setOnClickListener(v -> listener.onShare(record));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(record);
            return true;
        });
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
        final View share;

        Holder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_apk_name);
            detail = itemView.findViewById(R.id.text_apk_detail);
            share = itemView.findViewById(R.id.btn_apk_share);
        }
    }
}
