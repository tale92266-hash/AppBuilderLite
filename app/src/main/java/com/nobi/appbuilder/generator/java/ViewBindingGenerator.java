package com.nobi.appbuilder.generator.java;

import com.nobi.appbuilder.generator.mapping.ComponentMappingRegistry;
import com.nobi.appbuilder.generator.xml.XMLIdResolver;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Collects the components a generated Activity actually needs
 * references to — every component with events (its listeners need
 * the view) — and emits the field declarations plus the
 * findViewById lines. Components with no events get no binding, so
 * generated code stays tight and every binding is used.
 */
public class ViewBindingGenerator {

    /** component id -> Java class, in document order. */
    public Map<String, String> collectBindings(ComponentModel root) {
        Map<String, String> bindings = new LinkedHashMap<>();
        collect(root, bindings);
        return bindings;
    }

    private void collect(ComponentModel model, Map<String, String> bindings) {
        if (!model.getEvents().isEmpty()) {
            String javaClass = ComponentMappingRegistry.javaClassFor(model.getType());
            if (javaClass != null) {
                bindings.put(model.getId(), javaClass);
            }
        }
        for (ComponentModel child : model.getChildren()) {
            collect(child, bindings);
        }
    }

    /** "    private Button button1;\n..." for the binding map. */
    public String fieldsFor(Map<String, String> bindings) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String> entry : bindings.entrySet()) {
            out.append("    private ").append(entry.getValue()).append(" ")
                    .append(XMLIdResolver.fieldNameFor(entry.getKey()))
                    .append(";\n");
        }
        return out.toString();
    }

    /** Emits one findViewById line per binding, e.g. a Button field
     *  bound to its generated view id. */
    public String bindingsFor(Map<String, String> bindings,
                              java.util.Map<String, String> resolvedIds) {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<String, String> entry : bindings.entrySet()) {
            String id = resolvedIds.get(entry.getKey());
            if (id == null) {
                id = XMLIdResolver.sanitizeId(entry.getKey());
            }
            out.append("        ").append(XMLIdResolver.fieldNameFor(entry.getKey()))
                    .append(" = findViewById(R.id.").append(id).append(");\n");
        }
        return out.toString();
    }
}
