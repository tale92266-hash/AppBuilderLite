package com.nobi.appbuilder.ui.flow;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionRegistry;
import com.nobi.appbuilder.flow.FlowNode;
import com.nobi.appbuilder.flow.FlowNodeType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Edits the selected flow node: label, action (ACTION nodes only)
 * and parameters as key=value lines. Apply writes back into the node
 * and reports through the listener; nothing is saved to disk here.
 */
public class FlowPropertiesController {

    public interface FlowPropertiesListener {
        void onNodeChanged(FlowNode node);
    }

    private final View root;
    private final FlowPropertiesListener listener;
    private FlowNode current;

    public FlowPropertiesController(View root, FlowPropertiesListener listener) {
        this.root = root;
        this.listener = listener;
        Button apply = root.findViewById(R.id.btn_flow_node_apply);
        if (apply != null) {
            apply.setOnClickListener(v -> applyChanges());
        }
    }

    public void show(FlowNode node) {
        this.current = node;
        if (node == null) {
            root.setVisibility(View.GONE);
            return;
        }
        root.setVisibility(View.VISIBLE);
        TextView label = root.findViewById(R.id.text_flow_node_label);
        if (label != null) {
            label.setText(node.getType().displayName() + " node");
        }
        EditText labelInput = root.findViewById(R.id.input_flow_node_label);
        if (labelInput != null) {
            labelInput.setText(node.getLabel() == null ? "" : node.getLabel());
        }
        Spinner actionSpinner = root.findViewById(R.id.spinner_flow_node_action);
        if (actionSpinner != null) {
            List<String> ids = new ArrayList<>();
            ids.add("");
            for (ActionDefinition def : ActionRegistry.getAll()) {
                ids.add(def.getId());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    root.getContext(), android.R.layout.simple_spinner_item,
                    ids);
            adapter.setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item);
            actionSpinner.setAdapter(adapter);
            actionSpinner.setVisibility(
                    node.getType() == FlowNodeType.ACTION
                            ? View.VISIBLE : View.GONE);
            if (node.getActionId() != null) {
                int index = ids.indexOf(node.getActionId());
                if (index >= 0) {
                    actionSpinner.setSelection(index);
                }
            }
        }
        EditText paramsInput = root.findViewById(R.id.input_flow_node_params);
        if (paramsInput != null) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry
                    : node.getParams().entrySet()) {
                sb.append(entry.getKey()).append("=")
                        .append(entry.getValue()).append("\n");
            }
            paramsInput.setText(sb.toString().trim());
        }
    }

    public void hide() {
        current = null;
        root.setVisibility(View.GONE);
    }

    private void applyChanges() {
        if (current == null) {
            return;
        }
        EditText labelInput = root.findViewById(R.id.input_flow_node_label);
        if (labelInput != null) {
            current.setLabel(labelInput.getText().toString().trim());
        }
        Spinner actionSpinner = root.findViewById(R.id.spinner_flow_node_action);
        if (actionSpinner != null
                && current.getType() == FlowNodeType.ACTION) {
            Object selected = actionSpinner.getSelectedItem();
            if (selected != null) {
                current.setActionId(selected.toString());
            }
        }
        EditText paramsInput = root.findViewById(R.id.input_flow_node_params);
        if (paramsInput != null) {
            Map<String, String> params = new LinkedHashMap<>();
            for (String line : paramsInput.getText().toString().split("\n")) {
                int eq = line.indexOf('=');
                if (eq > 0) {
                    params.put(line.substring(0, eq).trim(),
                            line.substring(eq + 1).trim());
                }
            }
            current.setParams(params);
        }
        if (listener != null) {
            listener.onNodeChanged(current);
        }
    }
}
