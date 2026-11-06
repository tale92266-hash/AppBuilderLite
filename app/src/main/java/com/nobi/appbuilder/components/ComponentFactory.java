package com.nobi.appbuilder.components;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.model.LayoutModel;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.LinkedHashMap;

/**
 * Creates components from the registry: default properties, unique ids,
 * default dimensions and layout. Also deep-clones whole subtrees
 * (duplicate / copy-paste in the editor) with fresh ids everywhere.
 */
public final class ComponentFactory {

    private ComponentFactory() {
    }

    /** Creates a component of the given type with registry defaults. */
    public static ComponentModel create(String type) {
        return create(type, 0, 0);
    }

    /** Creates a component of the given type at canvas position (x, y) dp. */
    public static ComponentModel create(String type, int x, int y) {
        ComponentDefinition definition = ComponentRegistry.get(type);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown component type: " + type);
        }
        ComponentModel model = new ComponentModel(IDGenerator.newComponentId(), type);
        model.setX(x);
        model.setY(y);
        model.setWidth(definition.getDefaultWidthDp());
        model.setHeight(definition.getDefaultHeightDp());
        model.setProperties(definition.createDefaultProperties());
        LayoutModel layout = new LayoutModel(
                LayoutModel.dimensionToString(definition.getDefaultWidthDp()),
                LayoutModel.dimensionToString(definition.getDefaultHeightDp()));
        model.setLayout(layout);
        return model;
    }

    /** The root container every new screen starts with. */
    public static ComponentModel createRootContainer() {
        ComponentModel root = create(LinearLayoutComponent.TYPE);
        root.setWidth(ComponentModel.MATCH_PARENT);
        root.setHeight(ComponentModel.MATCH_PARENT);
        root.getLayout().setWidth(LayoutModel.MATCH_PARENT);
        root.getLayout().setHeight(LayoutModel.MATCH_PARENT);
        return root;
    }

    /** True when the registry knows this type. */
    public static boolean isValidType(String type) {
        return ComponentRegistry.contains(type);
    }

    /**
     * Deep-clones a component subtree. Every node and event gets a fresh
     * id, so the clone can live beside the original in the same document.
     */
    public static ComponentModel cloneTree(ComponentModel source) {
        if (source == null) {
            return null;
        }
        ComponentModel copy = new ComponentModel(
                IDGenerator.newComponentId(), source.getType());
        copy.setX(source.getX());
        copy.setY(source.getY());
        copy.setWidth(source.getWidth());
        copy.setHeight(source.getHeight());
        copy.setProperties(new LinkedHashMap<>(source.getProperties()));
        copy.setLayout(source.getLayout().copy());
        copy.setEditorMetadata(new LinkedHashMap<>(source.getEditorMetadata()));
        for (EventModel event : source.getEvents()) {
            copy.addEvent(new EventModel(
                    IDGenerator.newEventId(),
                    copy.getId(),
                    event.getEventType(),
                    event.getAction(),
                    event.getTargetScreenId(),
                    event.getMessage()));
        }
        for (ComponentModel child : source.getChildren()) {
            copy.addChild(cloneTree(child));
        }
        return copy;
    }
}
