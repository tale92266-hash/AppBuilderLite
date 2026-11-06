package com.nobi.appbuilder.codeproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Reads the workspace file tree. */
public class CodeFileRepository {

    private final File root;

    public CodeFileRepository(File root) {
        this.root = root;
    }

    public File getRoot() {
        return root;
    }

    /** The tree, directories first, alphabetical. */
    public List<CodeFileNode> loadTree() {
        List<CodeFileNode> out = new ArrayList<>();
        if (root == null || !root.isDirectory()) {
            return out;
        }
        loadInto(root, 0, out);
        return out;
    }

    private void loadInto(File dir, int depth, List<CodeFileNode> out) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        java.util.Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() != b.isDirectory()) {
                return a.isDirectory() ? -1 : 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });
        for (File file : files) {
            if (file.getName().startsWith(".")) {
                continue;
            }
            CodeFileNode node = new CodeFileNode(file, depth);
            out.add(node);
            if (file.isDirectory()) {
                loadInto(file, depth + 1, node.getChildren());
            }
        }
    }

    /** All text files under the root, for project-wide search. */
    public List<File> listTextFiles() {
        List<File> out = new ArrayList<>();
        collectText(root, out);
        return out;
    }

    private void collectText(File dir, List<File> out) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                collectText(file, out);
            } else if (CodeFileValidator.isTextFile(file)) {
                out.add(file);
            }
        }
    }
}
