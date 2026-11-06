package com.nobi.appbuilder.codeeditor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** The set of open tabs: documents plus the active one. */
public class EditorSession {

    public interface SessionListener {
        void onActiveChanged(EditorDocument document);

        void onDocumentClosed(EditorDocument document);

        void onDirtyChanged(EditorDocument document);
    }

    private final List<EditorDocument> documents = new ArrayList<>();
    private final List<SessionListener> listeners = new ArrayList<>();
    private int activeIndex = -1;

    public void addListener(SessionListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SessionListener listener) {
        listeners.remove(listener);
    }

    public EditorDocument open(EditorDocument document) {
        for (int i = 0; i < documents.size(); i++) {
            if (documents.get(i).getPath().equals(document.getPath())) {
                setActive(i);
                return documents.get(i);
            }
        }
        documents.add(document);
        setActive(documents.size() - 1);
        return document;
    }

    public void close(int index) {
        if (index < 0 || index >= documents.size()) {
            return;
        }
        EditorDocument removed = documents.remove(index);
        for (SessionListener listener : new ArrayList<>(listeners)) {
            listener.onDocumentClosed(removed);
        }
        if (documents.isEmpty()) {
            activeIndex = -1;
        } else if (activeIndex >= documents.size()) {
            setActive(documents.size() - 1);
        } else if (activeIndex == index) {
            setActive(Math.max(0, index - 1));
        }
    }

    public void close(EditorDocument document) {
        for (int i = 0; i < documents.size(); i++) {
            if (documents.get(i) == document) {
                close(i);
                return;
            }
        }
    }

    public void setActive(int index) {
        if (index < 0 || index >= documents.size()) {
            return;
        }
        activeIndex = index;
        for (SessionListener listener : new ArrayList<>(listeners)) {
            listener.onActiveChanged(documents.get(index));
        }
    }

    public EditorDocument getActive() {
        if (activeIndex < 0 || activeIndex >= documents.size()) {
            return null;
        }
        return documents.get(activeIndex);
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public List<EditorDocument> getDocuments() {
        return new ArrayList<>(documents);
    }

    public boolean hasUnsavedChanges() {
        for (EditorDocument document : documents) {
            if (document.isDirty()) {
                return true;
            }
        }
        return false;
    }

    public EditorDocument findByFile(File file) {
        for (EditorDocument document : documents) {
            if (document.getPath().equals(file.getAbsolutePath())) {
                return document;
            }
        }
        return null;
    }
}
