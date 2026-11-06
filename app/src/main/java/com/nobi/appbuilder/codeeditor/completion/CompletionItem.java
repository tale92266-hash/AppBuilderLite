package com.nobi.appbuilder.codeeditor.completion;

/** One completion suggestion. */
public class CompletionItem {

    public static final int KIND_KEYWORD = 0;
    public static final int KIND_CLASS = 1;
    public static final int KIND_METHOD = 2;
    public static final int KIND_ATTRIBUTE = 3;
    public static final int KIND_TAG = 4;
    public static final int KIND_RESOURCE = 5;

    private final String text;
    private final String detail;
    private final int kind;

    public CompletionItem(String text, String detail, int kind) {
        this.text = text;
        this.detail = detail;
        this.kind = kind;
    }

    public String getText() {
        return text;
    }

    public String getDetail() {
        return detail;
    }

    public int getKind() {
        return kind;
    }

    public String getKindLabel() {
        switch (kind) {
            case KIND_KEYWORD:
                return "keyword";
            case KIND_CLASS:
                return "class";
            case KIND_METHOD:
                return "method";
            case KIND_ATTRIBUTE:
                return "attribute";
            case KIND_TAG:
                return "tag";
            case KIND_RESOURCE:
                return "resource";
            default:
                return "";
        }
    }
}
