package com.nobi.appbuilder.library.custom;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.List;

/**
 * Facade for custom components: create from a selected subtree,
 * save (validated, versioned), instantiate, list, delete.
 */
public class CustomComponentManager {

    private final CustomComponentRepository repository;

    public CustomComponentManager(DatabaseHelper dbHelper) {
        this.repository = new CustomComponentRepository(dbHelper);
    }

    public CustomComponentRepository getRepository() {
        return repository;
    }

    /** Creates + validates + saves. Returns null with errors filled. */
    public CustomComponentDefinition create(String projectId, String name,
                                            String description,
                                            ComponentModel root,
                                            List<String> errors) {
        if (root == null) {
            errors.add("No component selected");
            return null;
        }
        CustomComponentDefinition definition =
                CustomComponentFactory.create(projectId, name, description, root);
        List<String> problems = CustomComponentValidator.validate(definition);
        if (!problems.isEmpty()) {
            errors.addAll(problems);
            return null;
        }
        if (!repository.save(definition)) {
            errors.add("Could not save the custom component");
            return null;
        }
        return definition;
    }

    public ComponentModel instantiate(String customComponentId) {
        CustomComponentDefinition definition =
                repository.getById(customComponentId);
        if (definition == null) {
            return null;
        }
        return new CustomComponentInstantiator().instantiate(definition, null);
    }

    public List<CustomComponentDefinition> list(String projectId) {
        return repository.getByProject(projectId);
    }

    public boolean delete(String customComponentId) {
        return repository.delete(customComponentId);
    }
}
