package com.nobi.appbuilder.screens;

import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.utils.IDGenerator;

/**
 * Duplicates a screen: new id, new name, deep-cloned component tree
 * (every id fresh), same type and orientation, never the launcher.
 */
public final class ScreenDuplicator {

    private ScreenDuplicator() {
    }

    public static ScreenModel duplicate(ScreenModel source, String newName,
                                        int sortOrder) {
        ScreenModel copy = new ScreenModel(IDGenerator.newScreenId(),
                source.getProjectId(), newName, false, sortOrder);
        copy.setScreenType(source.getScreenType());
        copy.setOrientation(source.getOrientation());
        if (source.getRootComponent() != null) {
            copy.setRootComponent(
                    ComponentFactory.cloneTree(source.getRootComponent()));
        } else {
            copy.setRootComponent(ComponentFactory.createRootContainer());
        }
        return copy;
    }
}
