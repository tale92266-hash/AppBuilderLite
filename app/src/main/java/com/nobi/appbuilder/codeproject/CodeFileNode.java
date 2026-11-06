package com.nobi.appbuilder.codeproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** One node of the project file tree. */
public class CodeFileNode {

    private final File file;
    private final int depth;
    private final List<CodeFileNode> children = new ArrayList<>();
    private boolean expanded;

    public CodeFileNode(File file, int depth) {
        this.file = file;
        this.depth = depth;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return file.getName();
    }

    public boolean isDirectory() {
        return file.isDirectory();
    }

    public int getDepth() {
        return depth;
    }

    public List<CodeFileNode> getChildren() {
        return children;
    }

    public void addChild(CodeFileNode child) {
        children.add(child);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public String getRelativePath(File root) {
        String rootPath = root.getAbsolutePath();
        String path = file.getAbsolutePath();
        return path.startsWith(rootPath)
                ? path.substring(rootPath.length() + 1) : file.getName();
    }
}
