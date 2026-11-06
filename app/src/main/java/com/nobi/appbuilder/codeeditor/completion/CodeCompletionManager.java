package com.nobi.appbuilder.codeeditor.completion;

import com.nobi.appbuilder.codeeditor.EditorLanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Aggregates providers; filters by the current token. */
public class CodeCompletionManager {

    private final List<CompletionProvider> providers = new ArrayList<>();
    private Set<String> projectClasses;
    private Set<String> resourceIds;

    public CodeCompletionManager() {
        providers.add(new JavaCompletionProvider());
        providers.add(new XmlCompletionProvider());
    }

    public void setProjectClasses(Set<String> projectClasses) {
        this.projectClasses = projectClasses;
    }

    public void setResourceIds(Set<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    /** Suggestions for the token at the cursor; at most 20. */
    public List<CompletionItem> complete(String text, int offset,
                                         EditorLanguage language) {
        List<CompletionItem> out = new ArrayList<>();
        for (CompletionProvider provider : providers) {
            if (!provider.supports(language)) {
                continue;
            }
            out.addAll(provider.complete(text, offset, projectClasses,
                    resourceIds));
            if (out.size() >= 20) {
                break;
            }
        }
        return out.size() > 20 ? out.subList(0, 20) : out;
    }
}
