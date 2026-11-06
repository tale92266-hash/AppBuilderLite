package com.nobi.appbuilder.library.custom;

import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/**
 * Creates a fresh instance of a custom component: the saved subtree
 * is cloned (fresh ids everywhere), then public-property overrides
 * are applied. Changing the definition never rewrites an existing
 * instance's overrides — instances are plain component trees.
 */
public class CustomComponentInstantiator {

    public ComponentModel instantiate(CustomComponentDefinition definition,
                                      Map<String, String> overrides) {
        if (definition == null || definition.getComponentsJson() == null) {
            return null;
        }
        ComponentModel saved;
        try {
            saved = IRDeserializer.deserializeComponent(
                    definition.getComponentsJson());
        } catch (Exception e) {
            return null;
        }
        if (saved == null) {
            return null;
        }
        ComponentModel instance = ComponentFactory.cloneTree(saved);
        if (overrides != null) {
            for (CustomComponentDefinition.PublicProperty property
                    : definition.getPublicProperties()) {
                String value = overrides.get(property.name);
                if (value == null) {
                    continue;
                }
                ComponentModel target = findBySavedId(instance, saved,
                        property.targetComponentId);
                if (target != null) {
                    target.setProperty(property.propertyKey, value);
                }
            }
        }
        return instance;
    }

    /**
     * cloneTree assigns fresh ids, so the target is found by walking
     * both trees in the same order.
     */
    private ComponentModel findBySavedId(ComponentModel instanceRoot,
                                         ComponentModel savedRoot,
                                         String savedId) {
        if (savedRoot.getId().equals(savedId)) {
            return instanceRoot;
        }
        for (int i = 0; i < savedRoot.getChildren().size()
                && i < instanceRoot.getChildren().size(); i++) {
            ComponentModel found = findBySavedId(
                    instanceRoot.getChildren().get(i),
                    savedRoot.getChildren().get(i), savedId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
