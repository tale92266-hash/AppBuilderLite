package com.nobi.appbuilder.ui.code;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;

import java.util.ArrayList;
import java.util.List;

/**
 * File explorer for generated code: directory section headers plus
 * file rows. A row shows a red dot when the generation report lists
 * an error for that file. Generated files are read-only.
 */
public class CodeFileAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnFileSelectedListener {
        void onFileSelected(String path);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_FILE = 1;

    /** One visible row: either a directory header or a file. */
    public static class Row {
        public final boolean isHeader;
        public final String label;
        public final String path;
        public final boolean hasError;

        public Row(boolean isHeader, String label, String path,
                   boolean hasError) {
            this.isHeader = isHeader;
            this.label = label;
            this.path = path;
            this.hasError = hasError;
        }
    }

    private final List<Row> rows = new ArrayList<>();
    private final OnFileSelectedListener listener;
    private String selectedPath;

    public CodeFileAdapter(OnFileSelectedListener listener) {
        this.listener = listener;
    }

    /** Builds header/file rows from the sorted file list. */
    public void setFiles(List<String> paths, java.util.Set<String> errorFiles) {
        rows.clear();
        String currentDir = null;
        for (String path : paths) {
            int slash = path.lastIndexOf('/');
            String dir = slash >= 0 ? path.substring(0, slash) : "";
            String name = slash >= 0 ? path.substring(slash + 1) : path;
            if (!dir.equals(currentDir)) {
                currentDir = dir;
                rows.add(new Row(true, dir.isEmpty() ? "/" : dir, null, false));
            }
            rows.add(new Row(false, name, path,
                    errorFiles != null && errorFiles.contains(path)));
        }
        selectedPath = null;
        notifyDataSetChanged();
    }

    public void setSelectedPath(String path) {
        selectedPath = path;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).isHeader ? TYPE_HEADER : TYPE_FILE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                      int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_code_header, parent, false);
            return new HeaderHolder(view);
        }
        View view = inflater.inflate(R.layout.item_code_file, parent, false);
        return new FileHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                                 int position) {
        Row row = rows.get(position);
        if (holder instanceof HeaderHolder) {
            ((HeaderHolder) holder).label.setText(row.label);
        } else {
            FileHolder fileHolder = (FileHolder) holder;
            fileHolder.label.setText(row.label);
            fileHolder.errorDot.setVisibility(
                    row.hasError ? View.VISIBLE : View.GONE);
            boolean selected = row.path != null
                    && row.path.equals(selectedPath);
            fileHolder.itemView.setBackgroundColor(selected
                    ? 0xFFE8EEF7 : 0x00000000);
            fileHolder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFileSelected(row.path);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        final TextView label;

        HeaderHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.text_code_header);
        }
    }

    static class FileHolder extends RecyclerView.ViewHolder {
        final TextView label;
        final View errorDot;

        FileHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.text_code_file);
            errorDot = itemView.findViewById(R.id.view_code_error_dot);
        }
    }
}
