package com.nobi.appbuilder.events;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Facade over the event system: list/add/update/remove events for a
 * component, and the screen-id set validators need. The EventModel
 * on the component stays the single source of truth.
 */
public class EventManager {

    private final DatabaseHelper dbHelper;
    private final EventBindingManager bindings;
    private final EventRepository repository;

    public EventManager(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.bindings = new EventBindingManager(dbHelper);
        this.repository = new EventRepository(dbHelper);
    }

    public List<EventDefinition> getEvents(String projectId, String screenId,
                                           String componentId) {
        return bindings.getEventsForComponent(projectId, screenId, componentId);
    }

    public List<EventDefinition> getScreenEvents(String projectId,
                                                 String screenId) {
        return bindings.getEventsForScreen(projectId, screenId);
    }

    public String addEvent(String projectId, String screenId,
                           String componentId, EventDefinition definition) {
        List<String> problems = EventValidator.validate(definition,
                getScreenIds(projectId));
        if (!problems.isEmpty()) {
            return problems.get(0);
        }
        return bindings.addEvent(projectId, screenId, componentId, definition);
    }

    public String updateEvent(String projectId, String screenId,
                              String componentId, EventDefinition definition) {
        List<String> problems = EventValidator.validate(definition,
                getScreenIds(projectId));
        if (!problems.isEmpty()) {
            return problems.get(0);
        }
        return bindings.updateEvent(projectId, screenId, componentId,
                definition);
    }

    public String removeEvent(String projectId, String screenId,
                              String componentId, String eventId) {
        String error = bindings.removeEvent(projectId, screenId, componentId,
                eventId);
        if (error == null) {
            repository.deleteFlowGraph(projectId, eventId);
        }
        return error;
    }

    public EventDefinition getEvent(String projectId, String screenId,
                                    String componentId, String eventId) {
        for (EventDefinition def : getEvents(projectId, screenId,
                componentId)) {
            if (eventId != null && eventId.equals(def.getId())) {
                return def;
            }
        }
        return null;
    }

    public boolean componentHasEvents(String projectId, String screenId,
                                      String componentId) {
        return !getEvents(projectId, screenId, componentId).isEmpty();
    }

    /** All screen ids of the project, for destination validation. */
    public Set<String> getScreenIds(String projectId) {
        Set<String> ids = new HashSet<>();
        IRDocument document = new DocumentRepository(dbHelper)
                .loadDocument(projectId);
        if (document == null) {
            return ids;
        }
        for (ScreenModel screen : document.getScreens()) {
            if (screen.getId() != null) {
                ids.add(screen.getId());
            }
        }
        return ids;
    }

    public List<String> validateAll(String projectId) {
        List<String> errors = new ArrayList<>();
        Set<String> screenIds = getScreenIds(projectId);
        IRDocument document = new DocumentRepository(dbHelper)
                .loadDocument(projectId);
        if (document == null) {
            return errors;
        }
        for (ScreenModel screen : document.getScreens()) {
            for (EventDefinition def : bindings.getEventsForScreen(
                    projectId, screen.getId())) {
                for (String problem : EventValidator.validate(def, screenIds)) {
                    errors.add(screen.getName() + ": " + problem);
                }
            }
        }
        return errors;
    }
}
