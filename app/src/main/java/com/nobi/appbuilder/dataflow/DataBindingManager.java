package com.nobi.appbuilder.dataflow;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Applies data bindings to a component tree: each binding copies one
 * value (from variables or screen arguments) into one component
 * property, with an optional fixed transform. Bindings that point at
 * a missing component or a missing value are reported, never applied
 * silently.
 */
public class DataBindingManager {

    /** Applies bindings; returns the problems found (empty = all applied). */
    public List<String> applyBindings(ComponentModel root,
                                      List<DataBindingDefinition> bindings,
                                      Map<String, String> values) {
        List<String> problems = new ArrayList<>();
        if (root == null || bindings == null) {
            return problems;
        }
        for (DataBindingDefinition binding : bindings) {
            if (binding.getSourceKey() == null
                    || binding.getTargetComponentId() == null
                    || binding.getTargetProperty() == null) {
                problems.add("binding '" + binding.getId()
                        + "' is missing source, target or property");
                continue;
            }
            String value = values == null
                    ? null : values.get(binding.getSourceKey());
            if (value == null) {
                problems.add("binding '" + binding.getId()
                        + "': no value for '" + binding.getSourceKey() + "'");
                continue;
            }
            ComponentModel target = root.findById(binding.getTargetComponentId());
            if (target == null) {
                problems.add("binding '" + binding.getId()
                        + "': component '" + binding.getTargetComponentId()
                        + "' not found");
                continue;
            }
            target.setProperty(binding.getTargetProperty(),
                    binding.applyTransform(value));
        }
        return problems;
    }

    /** Validates bindings against a component tree and known keys. */
    public List<String> validateBindings(ComponentModel root,
                                         List<DataBindingDefinition> bindings,
                                         List<String> knownKeys) {
        List<String> errors = new ArrayList<>();
        if (bindings == null) {
            return errors;
        }
        for (DataBindingDefinition binding : bindings) {
            if (binding.getSourceKey() == null
                    || binding.getSourceKey().isEmpty()) {
                errors.add("binding '" + binding.getId() + "' has no source key");
            } else if (knownKeys != null
                    && !knownKeys.contains(binding.getSourceKey())) {
                errors.add("binding '" + binding.getId() + "': unknown source '"
                        + binding.getSourceKey() + "'");
            }
            if (root != null && binding.getTargetComponentId() != null
                    && root.findById(binding.getTargetComponentId()) == null) {
                errors.add("binding '" + binding.getId() + "': target component '"
                        + binding.getTargetComponentId() + "' not found");
            }
            String transform = binding.getTransform();
            if (transform != null
                    && !DataBindingDefinition.TRANSFORM_NONE.equals(transform)
                    && !DataBindingDefinition.TRANSFORM_UPPERCASE.equals(transform)
                    && !DataBindingDefinition.TRANSFORM_LOWERCASE.equals(transform)) {
                errors.add("binding '" + binding.getId() + "': unknown transform '"
                        + transform + "'");
            }
        }
        return errors;
    }
}
