package com.nobi.appbuilder.ui.library;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.library.ComponentCategory;

import java.util.ArrayList;
import java.util.List;

/** Horizontal category chips for the library. */
public class ComponentCategoryAdapter
        extends RecyclerView.Adapter<ComponentCategoryAdapter.Holder> {

    public interface OnCategorySelectedListener {
        void onCategorySelected(ComponentCategory category);
    }

    private final List<ComponentCategory> categories = new ArrayList<>();
    private final OnCategorySelectedListener listener;
    private ComponentCategory selected = ComponentCategory.ALL;

    public ComponentCategoryAdapter(OnCategorySelectedListener listener) {
        this.listener = listener;
        for (ComponentCategory category : ComponentCategory.values()) {
            categories.add(category);
        }
    }

    public void setSelected(ComponentCategory category) {
        selected = category;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_component_category, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        ComponentCategory category = categories.get(position);
        holder.name.setText(category.getDisplayName());
        holder.name.setSelected(category == selected);
        holder.name.setBackgroundColor(category == selected
                ? 0xFFE8EEF7 : 0xFFF5F5F5);
        holder.itemView.setOnClickListener(v ->
                listener.onCategorySelected(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;

        Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_component_category_name);
        }
    }
}
