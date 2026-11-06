package com.nobi.appbuilder.codeeditor.completion;

import com.nobi.appbuilder.codeeditor.EditorLanguage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/** Java suggestions: keywords, Android classes, lifecycle methods,
 * project classes and resource ids. */
public class JavaCompletionProvider implements CompletionProvider {

    private static final List<String> KEYWORDS = Arrays.asList(
            "public", "private", "protected", "static", "final", "class",
            "void", "int", "boolean", "String", "return", "new", "if",
            "else", "for", "while", "try", "catch", "extends", "implements",
            "import", "package", "this", "super", "override", "true",
            "false", "null");
    private static final List<String> CLASSES = Arrays.asList(
            "Activity", "AppCompatActivity", "Bundle", "View", "Button",
            "TextView", "EditText", "ImageView", "LinearLayout",
            "FrameLayout", "ConstraintLayout", "ScrollView", "CheckBox",
            "Switch", "ProgressBar", "RecyclerView", "Intent", "Toast",
            "Context", "LayoutInflater", "ViewGroup", "ArrayAdapter",
            "SharedPreferences", "Handler", "Runnable");
    private static final List<String> METHODS = Arrays.asList(
            "onCreate", "onStart", "onResume", "onPause", "onStop",
            "onDestroy", "onClick", "setContentView", "findViewById",
            "setText", "getText", "setOnClickListener", "setVisibility",
            "setEnabled", "makeText", "startActivity", "finish",
            "getSystemService", "setBackgroundColor", "setTextColor");

    @Override
    public boolean supports(EditorLanguage language) {
        return language == EditorLanguage.JAVA;
    }

    @Override
    public List<CompletionItem> complete(String text, int offset,
                                         Set<String> projectClasses,
                                         Set<String> resourceIds) {
        String prefix = currentToken(text, offset);
        List<CompletionItem> out = new ArrayList<>();
        for (String keyword : KEYWORDS) {
            if (keyword.startsWith(prefix)) {
                out.add(new CompletionItem(keyword, "Java keyword",
                        CompletionItem.KIND_KEYWORD));
            }
        }
        for (String cls : CLASSES) {
            if (cls.toLowerCase().startsWith(prefix.toLowerCase())) {
                out.add(new CompletionItem(cls, "android class",
                        CompletionItem.KIND_CLASS));
            }
        }
        for (String method : METHODS) {
            if (method.startsWith(prefix)) {
                out.add(new CompletionItem(method, "method",
                        CompletionItem.KIND_METHOD));
            }
        }
        if (projectClasses != null) {
            for (String cls : projectClasses) {
                if (cls.toLowerCase().startsWith(prefix.toLowerCase())) {
                    out.add(new CompletionItem(cls, "project class",
                            CompletionItem.KIND_CLASS));
                }
            }
        }
        if (resourceIds != null) {
            for (String id : resourceIds) {
                String ref = "R.id." + id;
                if (ref.toLowerCase().startsWith(prefix.toLowerCase())) {
                    out.add(new CompletionItem(ref, "resource id",
                            CompletionItem.KIND_RESOURCE));
                }
            }
        }
        return out;
    }

    /** The identifier fragment directly before the cursor. */
    public static String currentToken(String text, int offset) {
        if (text == null || offset <= 0) {
            return "";
        }
        int end = Math.min(offset, text.length());
        int start = end;
        while (start > 0) {
            char c = text.charAt(start - 1);
            if (Character.isJavaIdentifierPart(c) || c == '.') {
                start--;
            } else {
                break;
            }
        }
        String token = text.substring(start, end);
        int dot = token.lastIndexOf('.');
        return dot >= 0 ? token.substring(dot + 1) : token;
    }
}
