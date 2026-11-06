package com.nobi.appbuilder.ui.editor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.history.CommandManager;
import com.nobi.appbuilder.history.CommandTransaction;
import com.nobi.appbuilder.history.UpdatePropertyCommand;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.properties.PropertyChangeListener;
import com.nobi.appbuilder.properties.PropertyController;
import com.nobi.appbuilder.properties.PropertyDefinition;
import com.nobi.appbuilder.properties.PropertyGroup;
import com.nobi.appbuilder.properties.PropertyValue;
import com.nobi.appbuilder.properties.editors.BooleanPropertyEditor;
import com.nobi.appbuilder.properties.editors.ColorPropertyEditor;
import com.nobi.appbuilder.properties.editors.DimensionPropertyEditor;
import com.nobi.appbuilder.properties.editors.EnumPropertyEditor;
import com.nobi.appbuilder.properties.editors.NumberPropertyEditor;
import com.nobi.appbuilder.properties.editors.ResourcePropertyEditor;
import com.nobi.appbuilder.properties.editors.TextPropertyEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * The properties inspector: a slide-over panel on the right of the
 * canvas. Selecting a component builds grouped, schema-driven rows;
 * every edit is validated, written to the model, and reported to the
 * activity (canvas refresh + autosave). The panel never rebuilds
 * itself on an edit, so scroll position and focus survive typing.
 */
public class PropertiesPanelController {

    public interface PropertiesListener {
        void onPropertyChangeStarting();

        void onPropertyChanged();

        void onPropertyValidationFailed(PropertyDefinition definition, String error);
    }

    private static class Row {
        boolean header;
        String headerTitle;
        PropertyDefinition definition;
        PropertyController.Editor editor;
        View editorView;
        TextView errorView;
    }

    private final View panel;
    private final RecyclerView recyclerView;
    private final TextView titleView;
    private final PropertiesListener listener;
    private final PropertyController propertyController;
    private final PropertiesAdapter adapter = new PropertiesAdapter();
    private final List<Row> rows = new ArrayList<>();
    private ComponentModel currentModel;
    private ComponentModel root;
    /** Phase 5: when set, property edits run as UpdatePropertyCommands. */
    private CommandManager.CommandRunner commandRunner;

    public void setCommandRunner(CommandManager.CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public PropertiesPanelController(View panel, RecyclerView recyclerView,
                                     TextView titleView, Button resetButton,
                                     View closeButton, PropertiesListener listener) {
        this.panel = panel;
        this.recyclerView = recyclerView;
        this.titleView = titleView;
        this.listener = listener;
        this.propertyController = new PropertyController(new PropertyChangeListener() {
            @Override
            public void onPropertyChanged(ComponentModel model,
                                          PropertyDefinition definition,
                                          PropertyValue oldValue, PropertyValue newValue) {
                listener.onPropertyChanged();
            }

            @Override
            public void onPropertyValidationFailed(PropertyDefinition definition,
                                                   String error) {
                listener.onPropertyValidationFailed(definition, error);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(adapter);
        resetButton.setOnClickListener(v -> resetToDefaults());
        closeButton.setOnClickListener(v -> hide());
    }

    public boolean isVisible() {
        return panel.getVisibility() == View.VISIBLE;
    }

    public void show(ComponentModel model, ComponentModel root) {
        this.currentModel = model;
        this.root = root;
        titleView.setText(model.getType() + " — " + model.getId());
        buildRows();
        panel.setVisibility(View.VISIBLE);
    }

    public void hide() {
        currentModel = null;
        root = null;
        panel.setVisibility(View.GONE);
    }

    /** Re-reads values into the existing editors (keeps scroll/focus). */
    public void refreshValues() {
        if (currentModel == null) {
            return;
        }
        for (Row row : rows) {
            if (row.editor != null) {
                row.editor.setValue(
                        propertyController.getValue(currentModel, row.definition));
            }
        }
        titleView.setText(currentModel.getType() + " — " + currentModel.getId());
    }

    private void resetToDefaults() {
        if (currentModel == null) {
            return;
        }
        if (commandRunner != null) {
            CommandTransaction transaction =
                    new CommandTransaction("Reset properties");
            for (PropertyDefinition definition :
                    propertyController.getDefinitionsFor(currentModel, root)) {
                if (!definition.isEditable()
                        || "id".equals(definition.getKey())
                        || "type".equals(definition.getKey())) {
                    continue;
                }
                PropertyValue current =
                        propertyController.getValue(currentModel, definition);
                PropertyValue fallback = definition.getDefaultValue();
                if (current == null || fallback == null || current.equals(fallback)) {
                    continue;
                }
                transaction.add(new UpdatePropertyCommand(currentModel, root,
                        definition, current, fallback));
            }
            if (!transaction.isEmpty()) {
                commandRunner.run(transaction);
            }
            buildRows();
            listener.onPropertyChanged();
            return;
        }
        listener.onPropertyChangeStarting();
        propertyController.resetToDefaults(currentModel, root);
        buildRows();
        listener.onPropertyChanged();
    }

    private void buildRows() {
        rows.clear();
        if (currentModel == null) {
            adapter.notifyDataSetChanged();
            return;
        }
        List<PropertyDefinition> definitions =
                propertyController.getDefinitionsFor(currentModel, root);
        PropertyGroup currentGroup = null;
        for (PropertyDefinition definition : definitions) {
            if (definition.getGroup() != currentGroup) {
                currentGroup = definition.getGroup();
                Row header = new Row();
                header.header = true;
                header.headerTitle = currentGroup.getDisplayName();
                rows.add(header);
            }
            Row row = new Row();
            row.definition = definition;
            if (definition.isEditable()) {
                PropertyValue initial =
                        propertyController.getValue(currentModel, definition);
                PropertyController.ValueListener valueListener = newValue -> {
                    if (commandRunner != null) {
                        PropertyValue oldValue =
                                propertyController.getValue(currentModel, definition);
                        UpdatePropertyCommand command = new UpdatePropertyCommand(
                                currentModel, root, definition, oldValue, newValue);
                        if (!command.canExecute()) {
                            showRowError(definition, command.getValidationError());
                            refreshValues();
                        } else {
                            clearRowError(definition);
                            commandRunner.run(command);
                            listener.onPropertyChanged();
                        }
                        return;
                    }
                    listener.onPropertyChangeStarting();
                    String error = propertyController.setValue(
                            currentModel, definition, newValue, root);
                    if (error != null) {
                        showRowError(definition, error);
                    } else {
                        clearRowError(definition);
                    }
                    listener.onPropertyChanged();
                };
                row.editor = createEditor(definition, initial, valueListener);
            }
            rows.add(row);
        }
        adapter.notifyDataSetChanged();
    }

    private PropertyController.Editor createEditor(PropertyDefinition definition,
                                                   PropertyValue initial,
                                                   PropertyController.ValueListener listener) {
        switch (definition.getType()) {
            case COLOR:
                return new ColorPropertyEditor(definition, initial, listener);
            case DIMENSION:
                return new DimensionPropertyEditor(definition, initial, listener);
            case BOOLEAN:
                return new BooleanPropertyEditor(definition, initial, listener);
            case ENUM:
            case GRAVITY:
            case ALIGNMENT:
                return new EnumPropertyEditor(definition, initial, listener);
            case INTEGER:
            case FLOAT:
                return new NumberPropertyEditor(definition, initial, listener);
            case RESOURCE:
                return new ResourcePropertyEditor(definition, initial, listener);
            case STRING:
            default:
                return new TextPropertyEditor(definition, initial, listener);
        }
    }

    private void showRowError(PropertyDefinition definition, String error) {
        for (Row row : rows) {
            if (row.definition == definition && row.errorView != null) {
                row.errorView.setText(error);
                row.errorView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void clearRowError(PropertyDefinition definition) {
        for (Row row : rows) {
            if (row.definition == definition && row.errorView != null) {
                row.errorView.setVisibility(View.GONE);
            }
        }
    }

    private class PropertiesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_HEADER = 0;
        private static final int VIEW_PROPERTY = 1;

        @Override
        public int getItemViewType(int position) {
            return rows.get(position).header ? VIEW_HEADER : VIEW_PROPERTY;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                          int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == VIEW_HEADER) {
                View view = inflater.inflate(R.layout.item_property_header, parent, false);
                return new HeaderHolder(view);
            }
            View view = inflater.inflate(R.layout.item_property_row, parent, false);
            return new PropertyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Row row = rows.get(position);
            if (holder instanceof HeaderHolder) {
                ((HeaderHolder) holder).title.setText(row.headerTitle);
                return;
            }
            PropertyHolder propertyHolder = (PropertyHolder) holder;
            propertyHolder.label.setText(row.definition.getDisplayName());
            propertyHolder.container.removeAllViews();
            if (row.editor != null) {
                if (row.editorView == null) {
                    row.editorView = row.editor.createRow(holder.itemView.getContext());
                }
                if (row.editorView.getParent() != null) {
                    ((ViewGroup) row.editorView.getParent()).removeView(row.editorView);
                }
                propertyHolder.container.addView(row.editorView);
            } else {
                TextView valueView = new TextView(holder.itemView.getContext());
                valueView.setText(propertyController.getValue(currentModel,
                        row.definition).asString());
                propertyHolder.container.addView(valueView);
            }
            row.errorView = propertyHolder.error;
            propertyHolder.error.setVisibility(View.GONE);
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        class HeaderHolder extends RecyclerView.ViewHolder {
            final TextView title;

            HeaderHolder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.text_property_header);
            }
        }

        class PropertyHolder extends RecyclerView.ViewHolder {
            final TextView label;
            final ViewGroup container;
            final TextView error;

            PropertyHolder(View itemView) {
                super(itemView);
                label = itemView.findViewById(R.id.text_property_label);
                container = itemView.findViewById(R.id.layout_property_editor);
                error = itemView.findViewById(R.id.text_property_error);
            }
        }
    }
}
