package com.nobi.appbuilder.ui.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.library.ComponentLibraryItem;

import java.util.ArrayList;
import java.util.List;

/** Library cards: name, description, favorite star, Add button. */
public class ComponentListAdapter
        extends RecyclerView.Adapter<ComponentListAdapter.Holder> {

    public interface OnComponentActionListener {
        void onOpenDetails(ComponentLibraryItem item);

        void onAdd(ComponentLibraryItem item);

        void onToggleFavorite(ComponentLibraryItem item);
    }

    private final List<ComponentLibraryItem> items = new ArrayList<>();
    private final OnComponentActionListener listener;

    public ComponentListAdapter(OnComponentActionListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ComponentLibraryItem> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library_component, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ComponentLibraryItem item = items.get(position);
        holder.name.setText(item.getDisplayName());
        String detail = item.getCategory().getDisplayName() + " · "
                + item.getBaseType();
        if (!item.isSupported()) {
            detail += " · " + item.getUnsupportedReason();
        } else {
            detail += "\n" + item.getDescription();
        }
        holder.detail.setText(detail);
        holder.badge.setVisibility(item.isCustom() ? View.VISIBLE : View.GONE);
        holder.badge.setText("Custom");
        holder.favorite.setText(item.isFavorite() ? "★" : "☆");
        holder.add.setEnabled(item.isSupported());
        holder.itemView.setOnClickListener(v -> listener.onOpenDetails(item));
        holder.add.setOnClickListener(v -> listener.onAdd(item));
        holder.favorite.setOnClickListener(v ->
                listener.onToggleFavorite(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView detail;
        final TextView badge;
        final TextView favorite;
        final Button add;

        Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_library_component_name);
            detail = itemView.findViewById(R.id.text_library_component_detail);
            badge = itemView.findViewById(R.id.text_library_component_badge);
            favorite = itemView.findViewById(R.id.btn_library_component_favorite);
            add = itemView.findViewById(R.id.btn_library_component_add);
        }
    }
}
