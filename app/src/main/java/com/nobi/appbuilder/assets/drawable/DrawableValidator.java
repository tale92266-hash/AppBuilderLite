package com.nobi.appbuilder.assets.drawable;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

/** Validates generated drawable XML: well-formed + right root tag. */
public final class DrawableValidator {

    private DrawableValidator() {
    }

    public static List<String> validate(String xml, String expectedRoot) {
        List<String> errors = new ArrayList<>();
        if (xml == null || xml.trim().isEmpty()) {
            errors.add("Drawable XML is empty");
            return errors;
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setNamespaceAware(true);
            org.w3c.dom.Document document = factory.newDocumentBuilder().parse(
                    new ByteArrayInputStream(
                            xml.getBytes(StandardCharsets.UTF_8)));
            String root = document.getDocumentElement().getTagName();
            if (expectedRoot != null && !expectedRoot.equals(root)) {
                errors.add("Expected root <" + expectedRoot + "> but found <"
                        + root + ">");
            }
        } catch (Exception e) {
            errors.add("Invalid drawable XML: " + e.getMessage());
        }
        return errors;
    }
}
