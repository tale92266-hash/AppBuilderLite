package com.nobi.appbuilder.ui.build;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.build.logs.BuildLogEntry;
import com.nobi.appbuilder.build.logs.BuildLogFilter;

import java.util.ArrayList;
import java.util.List;

/** Live build log list with level filtering and text search. */
public class BuildLogController {

    public interface LogActionListener {
        void onCopyLogs();

        void onExportLogs();
    }

    private final List<BuildLogEntry> allEntries = new ArrayList<>();
    private final List<BuildLogEntry> shownEntries = new ArrayList<>();
    private final BuildLogFilter filter = new BuildLogFilter();
    private final LogAdapter adapter = new LogAdapter();
    private final RecyclerView recyclerView;

    public BuildLogController(View root, LogActionListener listener) {
        recyclerView = root.findViewById(R.id.recycler_build_log);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                root.getContext()));
        recyclerView.setAdapter(adapter);
        EditText search = root.findViewById(R.id.input_build_log_search);
        Button filterErrors = root.findViewById(R.id.btn_build_log_errors);
        Button filterAll = root.findViewById(R.id.btn_build_log_all);
        filterErrors.setOnClickListener(v -> {
            filter.setShowInfo(false);
            filter.setShowDebug(false);
            filter.setShowSuccess(false);
            filter.setQuery(search.getText().toString());
            refresh();
        });
        filterAll.setOnClickListener(v -> {
            filter.setShowInfo(true);
            filter.setShowDebug(false);
            filter.setShowSuccess(true);
            filter.setQuery(search.getText().toString());
            refresh();
        });
        search.setOnEditorActionListener((v, actionId, event) -> {
            filter.setQuery(search.getText().toString());
            refresh();
            return true;
        });
        root.findViewById(R.id.btn_build_log_copy)
                .setOnClickListener(v -> listener.onCopyLogs());
        root.findViewById(R.id.btn_build_log_export)
                .setOnClickListener(v -> listener.onExportLogs());
    }

    public void show() {
        recyclerView.setVisibility(View.VISIBLE);
    }

    public void addEntry(BuildLogEntry entry) {
        allEntries.add(entry);
        refresh();
    }

    public void clear() {
        allEntries.clear();
        refresh();
    }

    public List<BuildLogEntry> getAllEntries() {
        return new ArrayList<>(allEntries);
    }

    private void refresh() {
        shownEntries.clear();
        shownEntries.addAll(filter.apply(allEntries));
        adapter.notifyDataSetChanged();
        if (!shownEntries.isEmpty()) {
            recyclerView.scrollToPosition(shownEntries.size() - 1);
        }
    }

    private class LogAdapter extends RecyclerView.Adapter<LogAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_build_log, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            BuildLogEntry entry = shownEntries.get(position);
            holder.text.setText(entry.getText());
            int color;
            switch (entry.getLevel()) {
                case BuildLogEntry.ERROR:
                    color = 0xFFB3261E;
                    break;
                case BuildLogEntry.WARNING:
                    color = 0xFF8D6E00;
                    break;
                case BuildLogEntry.SUCCESS:
                    color = 0xFF2E7D32;
                    break;
                default:
                    color = 0xFF111827;
            }
            holder.text.setTextColor(color);
        }

        @Override
        public int getItemCount() {
            return shownEntries.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView text;

            Holder(@NonNull View itemView) {
                super(itemView);
                text = itemView.findViewById(R.id.text_build_log_line);
            }
        }
    }
}
