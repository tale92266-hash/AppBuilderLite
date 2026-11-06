package com.nobi.appbuilder.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One node in a screen's component tree. All properties are stored as
 * strings (dp values, colours, text) so the model serialises cleanly to
 * JSON and maps 1:1 to Android XML attributes in Phase 7.
 * Width/height use Android constants: -1 = match_parent, -2 = wrap_content.
 */
public class ComponentModel {

    public static final int MATCH_PARENT = -1;
    public static final int WRAP_CONTENT = -2;

    private String id;
    private String type;
    private int x;
    private int y;
    private int width;
    private int height;
    private Map<String, String> properties;
    private List<ComponentModel> children;
    private List<EventModel> events;

    public ComponentModel() {
        properties = new LinkedHashMap<>();
        children = new ArrayList<>();
        events = new ArrayList<>();
    }

    public ComponentModel(String id, String type) {
        this();
        this.id = id;
        this.type = type;
        this.width = MATCH_PARENT;
        this.height = WRAP_CONTENT;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public String getProperty(String key, String fallback) {
        String value = properties.get(key);
        return value == null ? fallback : value;
    }

    public List<ComponentModel> getChildren() {
        return children;
    }

    public void setChildren(List<ComponentModel> children) {
        this.children = children;
    }

    public void addChild(ComponentModel child) {
        children.add(child);
    }

    public List<EventModel> getEvents() {
        return events;
    }

    public void setEvents(List<EventModel> events) {
        this.events = events;
    }

    public void addEvent(EventModel event) {
        events.add(event);
    }

    /** Depth-first search for a component by id, or null. */
    public ComponentModel findById(String searchId) {
        if (searchId != null && searchId.equals(id)) {
            return this;
        }
        for (ComponentModel child : children) {
            ComponentModel found = child.findById(searchId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
