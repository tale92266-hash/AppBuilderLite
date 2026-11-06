package com.nobi.appbuilder.codeeditor;

import java.io.File;

/** One open file: its text, language, dirty state and disk version. */
public class EditorDocument {

    private final File file;
    private final EditorLanguage language;
    private final EditorState state = new EditorState();
    private String text = "";
    private long diskLastModified;
    private long diskSize;

    public EditorDocument(File file, String text) {
        this.file = file;
        this.language = EditorLanguage.forFileName(file.getName());
        this.text = text == null ? "" : text;
        if (file.exists()) {
            this.diskLastModified = file.lastModified();
            this.diskSize = file.length();
        }
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return file.getName();
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    public EditorLanguage getLanguage() {
        return language;
    }

    public EditorState getState() {
        return state;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text == null ? "" : text;
        state.setDirty(true);
    }

    public boolean isDirty() {
        return state.isDirty();
    }

    public void markSaved() {
        state.setDirty(false);
        diskLastModified = file.lastModified();
        diskSize = file.length();
    }

    /** True when the file changed on disk since it was loaded/saved. */
    public boolean isExternallyModified() {
        return file.exists()
                && (file.lastModified() != diskLastModified
                || file.length() != diskSize);
    }

    public int lineCount() {
        if (text.isEmpty()) {
            return 1;
        }
        int count = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    public String lineText(int lineIndex) {
        String[] lines = text.split("\n", -1);
        if (lineIndex < 0 || lineIndex >= lines.length) {
            return "";
        }
        return lines[lineIndex];
    }

    /** Offset of the start of the given 0-based line. */
    public int lineStartOffset(int lineIndex) {
        int offset = 0;
        int line = 0;
        while (line < lineIndex && offset < text.length()) {
            if (text.charAt(offset) == '\n') {
                line++;
            }
            offset++;
        }
        return offset;
    }
}
