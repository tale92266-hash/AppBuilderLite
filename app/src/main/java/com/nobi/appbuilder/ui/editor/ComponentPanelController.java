package com.nobi.appbuilder.ui.editor;

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
import com.nobi.appbuilder.components.ComponentCategory;
import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.components.ComponentRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * The component library panel: searchable list of every registered
 * component, grouped by category. Tap arms tap-to-add; long-press
 * starts a real drag-and-drop onto the canvas.
 */
public class ComponentPanelController {

    public interface ComponentChosenListener {
        void onComponentTap(String type);

        boolean onComponentDragStart(View view, String type);
    }

    private static class Entry {
        boolean header;
        String headerTitle;
        String type;
        String name;

        static Entry header(String title) {
            Entry entry = new Entry();
            entry.header = true;
            entry.headerTitle = title;
            return entry;
        }

        static Entry item(ComponentDefinition definition) {
            Entry entry = new Entry();
            entry.type = definition.getType();
            entry.name = definition.getDisplayName();
            return entry;
        }
    }

    private final ComponentChosenListener listener;
    private final List<Entry> allEntries = new ArrayList<>();
    private final List<Entry> visibleEntries = new ArrayList<>();
    private final LibraryAdapter adapter = new LibraryAdapter();
    private String query = "";

    public ComponentPanelController(RecyclerView recyclerView, EditText searchView,
                                    ComponentChosenListener listener) {
        this.listener = listener;
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(adapter);
        buildEntries();
        applyFilter();
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s == null ? "" : s.toString().trim().toLowerCase();
                applyFilter();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void buildEntries() {
        ComponentCategory[] order = {
                ComponentCategory.LAYOUT, ComponentCategory.BASIC,
                ComponentCategory.INPUT, ComponentCategory.DISPLAY
        };
        for (ComponentCategory category : order) {
            List<ComponentDefinition> definitions =
                    ComponentRegistry.getByCategory(category);
            if (definitions.isEmpty()) {
                continue;
            }
            allEntries.add(Entry.header(categoryTitle(category)));
            for (ComponentDefinition definition : definitions) {
                allEntries.add(Entry.item(definition));
            }
        }
    }

    private String categoryTitle(ComponentCategory category) {
        switch (category) {
            case LAYOUT:
                return "Layouts";
            case BASIC:
                return "Basic";
            case INPUT:
                return "Inputs";
            case DISPLAY:
                return "Display";
            default:
                return category.name();
        }
    }

    private void applyFilter() {
        visibleEntries.clear();
        String currentHeader = null;
        List<Entry> pending = new ArrayList<>();
        for (Entry entry : allEntries) {
            if (entry.header) {
                flushHeader(currentHeader, pending);
                currentHeader = entry.headerTitle;
                pending = new ArrayList<>();
            } else if (query.isEmpty()
                    || entry.name.toLowerCase().contains(query)
                    || entry.type.toLowerCase().contains(query)) {
                pending.add(entry);
            }
        }
        flushHeader(currentHeader, pending);
        adapter.notifyDataSetChanged();
    }

    private void flushHeader(String header, List<Entry> items) {
        if (items.isEmpty()) {
            return;
        }
        if (header != null) {
            Entry headerEntry = Entry.header(header);
            visibleEntries.add(headerEntry);
        }
        visibleEntries.addAll(items);
    }

    private class LibraryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_HEADER = 0;
        private static final int VIEW_ITEM = 1;

        @Override
        public int getItemViewType(int position) {
            return visibleEntries.get(position).header ? VIEW_HEADER : VIEW_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                          int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_HEADER) {
                View view = inflater.inflate(R.layout.item_library_header, parent, false);
                return new HeaderHolder(view);
            }
            View view = inflater.inflate(R.layout.item_component_library, parent, false);
            return new ItemHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Entry entry = visibleEntries.get(position);
            if (holder instanceof HeaderHolder) {
                ((HeaderHolder) holder).title.setText(entry.headerTitle);
            } else {
                ItemHolder itemHolder = (ItemHolder) holder;
                itemHolder.name.setText(entry.name);
                itemHolder.thumb.setText(entry.name.isEmpty()
                        ? "?" : entry.name.substring(0, 1).toUpperCase());
                itemHolder.itemView.setOnClickListener(
                        v -> listener.onComponentTap(entry.type));
                itemHolder.itemView.setOnLongClickListener(
                        v -> listener.onComponentDragStart(v, entry.type));
            }
        }

        @Override
        public int getItemCount() {
            return visibleEntries.size();
        }

        class HeaderHolder extends RecyclerView.ViewHolder {
            final TextView title;

            HeaderHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.text_library_header);
            }
        }

        class ItemHolder extends RecyclerView.ViewHolder {
            final TextView thumb;
            final TextView name;

            ItemHolder(View itemView) {
                super(itemView);
                thumb = itemView.findViewById(R.id.text_component_thumb);
                name = itemView.findViewById(R.id.text_component_name);
            }
        }
    }
}
