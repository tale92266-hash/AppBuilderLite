package com.nobi.appbuilder.codeeditor.completion;

import com.nobi.appbuilder.codeeditor.EditorLanguage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/** XML suggestions: tags, Android attributes, resource references. */
public class XmlCompletionProvider implements CompletionProvider {

    private static final List<String> TAGS = Arrays.asList(
            "LinearLayout", "FrameLayout", "ConstraintLayout", "ScrollView",
            "TextView", "Button", "EditText", "ImageView", "CheckBox",
            "Switch", "ProgressBar", "RelativeLayout", "ImageButton",
            "RadioButton", "Spinner", "HorizontalScrollView",
            "androidx.recyclerview.widget.RecyclerView",
            "com.google.android.material.card.MaterialCardView");
    private static final List<String> ATTRIBUTES = Arrays.asList(
            "android:id", "android:layout_width", "android:layout_height",
            "android:text", "android:textSize", "android:textColor",
            "android:background", "android:orientation", "android:gravity",
            "android:padding", "android:layout_margin", "android:visibility",
            "android:hint", "android:src", "android:scaleType",
            "android:enabled", "android:inputType", "android:layout_weight",
            "android:layout_gravity", "android:checked",
            "android:max", "android:progress", "android:indeterminate",
            "android:contentDescription", "android:backgroundTint",
            "app:layout_constraintTop_toTopOf",
            "app:layout_constraintBottom_toBottomOf",
            "app:layout_constraintStart_toStartOf",
            "app:layout_constraintEnd_toEndOf",
            "app:cardCornerRadius", "app:cardBackgroundColor");

    @Override
    public boolean supports(EditorLanguage language) {
        return language == EditorLanguage.XML;
    }

    @Override
    public List<CompletionItem> complete(String text, int offset,
                                         Set<String> projectClasses,
                                         Set<String> resourceIds) {
        String prefix = currentToken(text, offset);
        List<CompletionItem> out = new ArrayList<>();
        boolean inTag = isInsideTag(text, offset);
        if (inTag) {
            for (String tag : TAGS) {
                if (tag.toLowerCase().startsWith(prefix.toLowerCase())) {
                    out.add(new CompletionItem(tag, "layout tag",
                            CompletionItem.KIND_TAG));
                }
            }
        }
        for (String attr : ATTRIBUTES) {
            if (attr.toLowerCase().startsWith(prefix.toLowerCase())) {
                out.add(new CompletionItem(attr, "Android attribute",
                        CompletionItem.KIND_ATTRIBUTE));
            }
        }
        if (resourceIds != null && prefix.startsWith("@")) {
            for (String id : resourceIds) {
                for (String kind : new String[]{"@string/", "@color/",
                        "@drawable/", "@dimen/"}) {
                    String ref = kind + id;
                    if (ref.startsWith(prefix)) {
                        out.add(new CompletionItem(ref, "resource",
                                CompletionItem.KIND_RESOURCE));
                    }
                }
            }
        }
        return out;
    }

    private boolean isInsideTag(String text, int offset) {
        if (text == null || offset <= 0) {
            return false;
        }
        int lt = text.lastIndexOf('<', offset - 1);
        int gt = text.lastIndexOf('>', offset - 1);
        return lt > gt;
    }

    private String currentToken(String text, int offset) {
        if (text == null || offset <= 0) {
            return "";
        }
        int end = Math.min(offset, text.length());
        int start = end;
        while (start > 0) {
            char c = text.charAt(start - 1);
            if (Character.isLetterOrDigit(c) || c == '_' || c == ':'
                    || c == '.' || c == '@' || c == '/') {
                start--;
            } else {
                break;
            }
        }
        return text.substring(start, end);
    }
}
