package com.nobi.appbuilder.events;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds events to components in the JSON IR: EventModel stays the
 * store, so the visual editor, preview and generator all see the
 * same events. Every mutation saves the document; failures are
 * returned as messages, never thrown.
 */
public class EventBindingManager {

    private final DocumentRepository documents;

    public EventBindingManager(DatabaseHelper dbHelper) {
        this.documents = new DocumentRepository(dbHelper);
    }

    /** Adds an event to a component. Returns null on success. */
    public String addEvent(String projectId, String screenId,
                           String componentId, EventDefinition definition) {
        if (definition == null) {
            return "no event given";
        }
        IRDocument document = documents.loadDocument(projectId);
        if (document == null) {
            return "project not found";
        }
        ComponentModel target = findComponent(document, screenId, componentId);
        if (target == null) {
            return "component not found";
        }
        if (definition.getId() == null || definition.getId().isEmpty()) {
            definition.setId(com.nobi.appbuilder.utils.IDGenerator
                    .newId("evt"));
        }
        definition.setComponentId(componentId);
        target.addEvent(definition.toEventModel());
        return documents.saveDocument(document) ? null : "could not save";
    }

    /** Replaces the event with the same id. Returns null on success. */
    public String updateEvent(String projectId, String screenId,
                              String componentId, EventDefinition definition) {
        if (definition == null || definition.getId() == null) {
            return "no event given";
        }
        IRDocument document = documents.loadDocument(projectId);
        if (document == null) {
            return "project not found";
        }
        ComponentModel target = findComponent(document, screenId, componentId);
        if (target == null) {
            return "component not found";
        }
        List<EventModel> events = target.getEvents();
        for (int i = 0; i < events.size(); i++) {
            if (definition.getId().equals(events.get(i).getId())) {
                events.set(i, definition.toEventModel());
                return documents.saveDocument(document)
                        ? null : "could not save";
            }
        }
        return "event not found";
    }

    /** Removes one event by id. Returns null on success. */
    public String removeEvent(String projectId, String screenId,
                              String componentId, String eventId) {
        IRDocument document = documents.loadDocument(projectId);
        if (document == null) {
            return "project not found";
        }
        ComponentModel target = findComponent(document, screenId, componentId);
        if (target == null) {
            return "component not found";
        }
        List<EventModel> events = target.getEvents();
        for (int i = 0; i < events.size(); i++) {
            if (eventId != null && eventId.equals(events.get(i).getId())) {
                events.remove(i);
                return documents.saveDocument(document)
                        ? null : "could not save";
            }
        }
        return "event not found";
    }

    public List<EventDefinition> getEventsForComponent(String projectId,
                                                       String screenId,
                                                       String componentId) {
        List<EventDefinition> out = new ArrayList<>();
        IRDocument document = documents.loadDocument(projectId);
        if (document == null) {
            return out;
        }
        ComponentModel target = findComponent(document, screenId, componentId);
        if (target == null) {
            return out;
        }
        for (EventModel event : target.getEvents()) {
            out.add(EventDefinition.fromEventModel(event));
        }
        return out;
    }

    /** All events on all components of one screen. */
    public List<EventDefinition> getEventsForScreen(String projectId,
                                                    String screenId) {
        List<EventDefinition> out = new ArrayList<>();
        IRDocument document = documents.loadDocument(projectId);
        if (document == null) {
            return out;
        }
        ScreenModel screen = document.getScreen(screenId);
        if (screen == null || screen.getRootComponent() == null) {
            return out;
        }
        collectEvents(screen.getRootComponent(), out);
        return out;
    }

    public boolean hasEvents(ComponentModel model) {
        return model != null && model.getEvents() != null
                && !model.getEvents().isEmpty();
    }

    private void collectEvents(ComponentModel model,
                               List<EventDefinition> out) {
        if (model == null) {
            return;
        }
        for (EventModel event : model.getEvents()) {
            out.add(EventDefinition.fromEventModel(event));
        }
        for (ComponentModel child : model.getChildren()) {
            collectEvents(child, out);
        }
    }

    private ComponentModel findComponent(IRDocument document, String screenId,
                                         String componentId) {
        ScreenModel screen = document.getScreen(screenId);
        if (screen == null || screen.getRootComponent() == null) {
            return null;
        }
        if (componentId == null) {
            return screen.getRootComponent();
        }
        return screen.getRootComponent().findById(componentId);
    }
}
