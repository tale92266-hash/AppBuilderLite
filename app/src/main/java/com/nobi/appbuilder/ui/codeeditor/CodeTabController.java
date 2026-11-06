package com.nobi.appbuilder.ui.codeeditor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.codeeditor.EditorDocument;

import java.util.ArrayList;
import java.util.List;

/** Horizontal tab strip for open files. */
public class CodeTabController {

    public interface TabListener {
        void onSelectTab(int index);
        void onCloseTab(int index);
    }

    private final TabListener listener;
    private final TabAdapter adapter = new TabAdapter();
    private List<EditorDocument> documents = new ArrayList<>();
    private int activeIndex = -1;

    public CodeTabController(View root, TabListener listener) {
        this.listener = listener;
        RecyclerView recycler = root.findViewById(R.id.recycler_code_tabs);
        recycler.setLayoutManager(new LinearLayoutManager(
                root.getContext(), LinearLayoutManager.HORIZONTAL, false));
        recycler.setAdapter(adapter);
    }

    public void setTabs(List<EditorDocument> documents, int activeIndex) {
        this.documents = new ArrayList<>(documents);
        this.activeIndex = activeIndex;
        adapter.notifyDataSetChanged();
    }

    private class TabAdapter extends RecyclerView.Adapter<TabAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_code_tab, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            EditorDocument document = documents.get(position);
            holder.name.setText(document.getFile().getName());
            holder.dirty.setVisibility(
                    document.getState().isDirty() ? View.VISIBLE
                            : View.GONE);
            holder.itemView.setBackgroundColor(position == activeIndex
                    ? 0xFFE8EEF7 : 0x00000000);
            holder.itemView.setOnClickListener(
                    v -> listener.onSelectTab(position));
            holder.close.setOnClickListener(
                    v -> listener.onCloseTab(position));
        }

        @Override
        public int getItemCount() {
            return documents.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView dirty;
            final View close;

            Holder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.text_code_tab_name);
                dirty = itemView.findViewById(R.id.text_code_tab_dirty);
                close = itemView.findViewById(R.id.btn_code_tab_close);
            }
        }
    }
}
