package com.nobi.appbuilder.ui.build;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.build.diagnostics.BuildError;
import com.nobi.appbuilder.build.diagnostics.BuildWarning;

import java.util.ArrayList;
import java.util.List;

/** Classified errors and warnings with their suggestions. */
public class BuildDiagnosticsController {

    private final View root;
    private final TextView emptyText;
    private final RecyclerView recyclerView;
    private final List<Object> items = new ArrayList<>();
    private final DiagnosticsAdapter adapter = new DiagnosticsAdapter();

    public BuildDiagnosticsController(View root) {
        this.root = root;
        emptyText = root.findViewById(R.id.text_build_diagnostics_empty);
        recyclerView = root.findViewById(R.id.recycler_build_diagnostics);
        recyclerView.setLayoutManager(new LinearLayoutManager(
                root.getContext()));
        recyclerView.setAdapter(adapter);
    }

    public void show(List<BuildError> errors, List<BuildWarning> warnings) {
        root.setVisibility(View.VISIBLE);
        items.clear();
        items.addAll(errors);
        items.addAll(warnings);
        emptyText.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        adapter.notifyDataSetChanged();
    }

    public void hide() {
        root.setVisibility(View.GONE);
    }

    private class DiagnosticsAdapter
            extends RecyclerView.Adapter<DiagnosticsAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_build_error, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            Object item = items.get(position);
            if (item instanceof BuildError) {
                BuildError error = (BuildError) item;
                holder.title.setText("Error · " + error.getCategory());
                holder.message.setText(error.getMessage());
                holder.suggestion.setText(error.getSuggestion());
            } else {
                BuildWarning warning = (BuildWarning) item;
                holder.title.setText("Warning · " + warning.getCategory());
                holder.message.setText(warning.getMessage());
                holder.suggestion.setText("");
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView message;
            final TextView suggestion;

            Holder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.text_build_error_title);
                message = itemView
                        .findViewById(R.id.text_build_error_message);
                suggestion = itemView
                        .findViewById(R.id.text_build_error_suggestion);
            }
        }
    }
}
