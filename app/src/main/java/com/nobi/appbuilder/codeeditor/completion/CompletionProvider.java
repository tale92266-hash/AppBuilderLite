package com.nobi.appbuilder.codeeditor.completion;

import com.nobi.appbuilder.codeeditor.EditorLanguage;

import java.util.List;
import java.util.Set;

/** Suggests completions for the token at the cursor. Offline only;
 * this is lightweight contextual completion, not IDE-level semantics. */
public interface CompletionProvider {

    boolean supports(EditorLanguage language);

    List<CompletionItem> complete(String text, int offset,
                                  Set<String> projectClasses,
                                  Set<String> resourceIds);
}
