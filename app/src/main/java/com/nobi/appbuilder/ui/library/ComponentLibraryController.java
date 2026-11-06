package com.nobi.appbuilder.ui.library;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.library.ComponentCategory;
import com.nobi.appbuilder.library.ComponentFilterManager;
import com.nobi.appbuilder.library.ComponentLibraryItem;
import com.nobi.appbuilder.library.ComponentSearchManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds the library screen: search field, category chips, component
 * list, empty state. Filtering is in-memory and instant.
 */
public class ComponentLibraryController {

    public interface LibraryListener {
        void onOpenDetails(ComponentLibraryItem item);

        void onAdd(ComponentLibraryItem item);

        void onToggleFavorite(ComponentLibraryItem item);
    }

    private final View root;
    private final LibraryListener listener;
    private final ComponentFilterManager filter = new ComponentFilterManager();
    private final ComponentListAdapter listAdapter;
    private final ComponentCategoryAdapter categoryAdapter;
    private final List<ComponentLibraryItem> allItems = new ArrayList<>();
    private String query = "";

    public ComponentLibraryController(View root, LibraryListener listener) {
        this.root = root;
        this.listener = listener;
        listAdapter = new ComponentListAdapter(
                new ComponentListAdapter.OnComponentActionListener() {
                    @Override
                    public void onOpenDetails(ComponentLibraryItem item) {
                        listener.onOpenDetails(item);
                    }

                    @Override
                    public void onAdd(ComponentLibraryItem item) {
                        listener.onAdd(item);
                    }

                    @Override
                    public void onToggleFavorite(ComponentLibraryItem item) {
                        listener.onToggleFavorite(item);
                    }
                });
        categoryAdapter = new ComponentCategoryAdapter(category -> {
            filter.setCategory(category);
            categoryAdapter.setSelected(category);
            refresh();
        });
        RecyclerView categories =
                root.findViewById(R.id.recycler_library_categories);
        categories.setLayoutManager(new LinearLayoutManager(
                root.getContext(), LinearLayoutManager.HORIZONTAL, false));
        categories.setAdapter(categoryAdapter);
        RecyclerView list = root.findViewById(R.id.recycler_library_components);
        list.setLayoutManager(new LinearLayoutManager(root.getContext()));
        list.setAdapter(listAdapter);
        EditText search = root.findViewById(R.id.input_library_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                query = s.toString();
                refresh();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setItems(List<ComponentLibraryItem> items) {
        allItems.clear();
        allItems.addAll(items);
        refresh();
    }

    public void setShowUnsupported(boolean show) {
        filter.setSupportedOnly(!show);
        refresh();
    }

    public void refresh() {
        List<ComponentLibraryItem> filtered =
                ComponentSearchManager.search(allItems, query);
        filtered = ComponentSearchManager.filterByCategory(filtered,
                filter.getCategory());
        filtered = filter.apply(filtered);
        listAdapter.setItems(filtered);
        TextView empty = root.findViewById(R.id.text_library_empty);
        empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
