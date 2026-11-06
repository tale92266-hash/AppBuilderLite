package com.nobi.appbuilder.sync.parser;

import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.generator.mapping.ComponentMappingRegistry;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.HashMap;
import java.util.Map;

/** Maps XML tags to component types (reverse of the generator). */
public class XMLComponentMapper {

    private static final Map<String, String> TAG_TO_TYPE = new HashMap<>();

    static {
        for (ComponentDefinition definition : ComponentRegistry.getAll()) {
            String tag = ComponentMappingRegistry
                    .xmlTagFor(definition.getType());
            if (tag != null) {
                TAG_TO_TYPE.put(tag, definition.getType());
            }
        }
    }

    public String typeForTag(String tag) {
        if (tag == null) {
            return null;
        }
        String type = TAG_TO_TYPE.get(tag);
        if (type != null) {
            return type;
        }
        // Fully-qualified tags (RecyclerView, MaterialCardView).
        int dot = tag.lastIndexOf('.');
        String simple = dot >= 0 ? tag.substring(dot + 1) : tag;
        for (Map.Entry<String, String> entry : TAG_TO_TYPE.entrySet()) {
            if (entry.getKey().endsWith("." + simple)
                    || entry.getKey().equals(simple)) {
                return entry.getValue();
            }
        }
        return null;
    }

    public boolean isSupportedTag(String tag) {
        return typeForTag(tag) != null;
    }

    /** Builds a bare ComponentModel (id + type) for a parsed element. */
    public ComponentModel toModel(XMLParseResult.ParsedElement element,
                                  String componentId) {
        String type = typeForTag(element.tag);
        if (type == null) {
            return null;
        }
        ComponentModel model = new ComponentModel();
        model.setId(componentId);
        model.setType(type);
        return model;
    }
}
