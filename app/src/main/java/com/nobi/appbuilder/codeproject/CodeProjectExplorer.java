package com.nobi.appbuilder.codeproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Explorer logic: tree, filename search, expand/collapse state. */
public class CodeProjectExplorer {

    private final CodeFileRepository repository;
    private final List<CodeFileNode> flatNodes = new ArrayList<>();

    public CodeProjectExplorer(File root) {
        this.repository = new CodeFileRepository(root);
    }

    public CodeFileRepository getRepository() {
        return repository;
    }

    /** Visible nodes after expand/collapse. */
    public List<CodeFileNode> getVisibleNodes() {
        flatNodes.clear();
        for (CodeFileNode node : repository.loadTree()) {
            flatten(node, flatNodes);
        }
        return new ArrayList<>(flatNodes);
    }

    private void flatten(CodeFileNode node, List<CodeFileNode> out) {
        out.add(node);
        if (node.isDirectory() && node.isExpanded()) {
            for (CodeFileNode child : node.getChildren()) {
                flatten(child, out);
            }
        }
    }

    public void toggleExpanded(CodeFileNode node) {
        node.setExpanded(!node.isExpanded());
    }

    /** Nodes whose name contains the query (case-insensitive). */
    public List<CodeFileNode> search(String query) {
        List<CodeFileNode> out = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getVisibleNodes();
        }
        String needle = query.trim().toLowerCase();
        searchInto(repository.loadTree(), needle, out);
        return out;
    }

    private void searchInto(List<CodeFileNode> nodes, String needle,
                            List<CodeFileNode> out) {
        for (CodeFileNode node : nodes) {
            if (node.getName().toLowerCase().contains(needle)) {
                out.add(node);
            }
            searchInto(node.getChildren(), needle, out);
        }
    }
}
