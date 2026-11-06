package com.nobi.appbuilder.codeeditor.completion;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;

import java.util.ArrayList;
import java.util.List;

/** Suggestion rows; tap inserts the suggestion. */
public class CompletionAdapter
        extends RecyclerView.Adapter<CompletionAdapter.Holder> {

    public interface OnSuggestionListener {
        void onSuggestion(CompletionItem item);
    }

    private final List<CompletionItem> items = new ArrayList<>();
    private final OnSuggestionListener listener;

    public CompletionAdapter(OnSuggestionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CompletionItem> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_code_completion, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        CompletionItem item = items.get(position);
        holder.text.setText(item.getText());
        holder.detail.setText(item.getDetail() + " · "
                + item.getKindLabel());
        holder.itemView.setOnClickListener(v -> listener.onSuggestion(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView text;
        final TextView detail;

        Holder(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text_completion_text);
            detail = itemView.findViewById(R.id.text_completion_detail);
        }
    }
}
