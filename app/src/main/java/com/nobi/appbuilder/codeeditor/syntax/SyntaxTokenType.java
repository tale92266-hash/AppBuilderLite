package com.nobi.appbuilder.codeeditor.syntax;

/** Token kinds produced by the state-aware tokenizers. */
public enum SyntaxTokenType {
    PLAIN,
    KEYWORD,
    TYPE,
    CLASS_NAME,
    METHOD,
    STRING,
    CHAR,
    NUMBER,
    COMMENT,
    ANNOTATION,
    OPERATOR,
    TAG,
    ATTRIBUTE,
    ATTR_VALUE,
    ENTITY,
    DECLARATION
}
