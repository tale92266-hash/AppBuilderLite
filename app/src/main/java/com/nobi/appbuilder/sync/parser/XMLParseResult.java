package com.nobi.appbuilder.sync.parser;

import java.util.ArrayList;
import java.util.List;

/** Result of parsing one layout XML file. */
public class XMLParseResult {

    /** One parsed element. */
    public static class ParsedElement {
        public final String tag;
        public final java.util.Map<String, String> attributes =
                new java.util.LinkedHashMap<>();
        public final List<ParsedElement> children = new ArrayList<>();

        public ParsedElement(String tag) {
            this.tag = tag;
        }

        public String attr(String name) {
            return attributes.get(name);
        }
    }

    private final ParsedElement root;
    private final List<XMLParseError> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public XMLParseResult(ParsedElement root) {
        this.root = root;
    }

    public ParsedElement getRoot() {
        return root;
    }

    public void addError(XMLParseError error) {
        errors.add(error);
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public List<XMLParseError> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean isSuccess() {
        return root != null && errors.isEmpty();
    }
}
