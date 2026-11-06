package com.nobi.appbuilder.ui.codeeditor;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.codeproject.CodeFileNode;
import com.nobi.appbuilder.codeproject.CodeProjectExplorer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** File tree panel: expand/collapse, filename search, open on tap. */
public class CodeFileExplorerController {

    public interface ExplorerListener {
        void onOpenFile(File file);
        void onDeleteFile(File file);
    }

    private final CodeProjectExplorer explorer;
    private final ExplorerListener listener;
    private final ExplorerAdapter adapter = new ExplorerAdapter();
    private List<CodeFileNode> nodes = new ArrayList<>();

    public CodeFileExplorerController(View root, File workspaceRoot,
                                      ExplorerListener listener) {
        this.explorer = new CodeProjectExplorer(workspaceRoot);
        this.listener = listener;
        RecyclerView recycler = root.findViewById(R.id.recycler_code_files);
        recycler.setLayoutManager(new LinearLayoutManager(
                root.getContext()));
        recycler.setAdapter(adapter);
        EditText search = root.findViewById(R.id.input_code_file_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                refresh(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        refresh("");
    }

    public void refresh() {
        refresh("");
    }

    public void refresh(String query) {
        nodes = query == null || query.isEmpty()
                ? explorer.getVisibleNodes() : explorer.search(query);
        adapter.notifyDataSetChanged();
    }

    private class ExplorerAdapter
            extends RecyclerView.Adapter<ExplorerAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_code_file, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            CodeFileNode node = nodes.get(position);
            String label = node.isDirectory()
                    ? (node.isExpanded() ? "\u25BE " : "\u25B8 ")
                    + node.getName() : node.getName();
            holder.name.setText(label);
            int pad = (int) (12 + node.getDepth() * 16
                    * holder.itemView.getResources()
                    .getDisplayMetrics().density);
            holder.itemView.setPadding(pad,
                    holder.itemView.getPaddingTop(),
                    holder.itemView.getPaddingEnd(),
                    holder.itemView.getPaddingBottom());
            holder.itemView.setOnClickListener(v -> {
                if (node.isDirectory()) {
                    explorer.toggleExpanded(node);
                    refresh("");
                } else {
                    listener.onOpenFile(node.getFile());
                }
            });
            holder.itemView.setOnLongClickListener(v -> {
                listener.onDeleteFile(node.getFile());
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return nodes.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView name;

            Holder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.text_code_file);
            }
        }
    }
}
