package com.nobi.appbuilder.generator.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Validates generated XML the hard way: a real namespace-aware
 * parse, duplicate android:id detection, and a check that every
 * @id/ reference inside the file resolves to an id the file defines.
 */
public final class XMLValidator {

    private XMLValidator() {
    }

    /** Parses the XML; returns null when valid, else the error. */
    public static String parse(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(null);
            builder.parse(new ByteArrayInputStream(
                    xml.getBytes(StandardCharsets.UTF_8)));
            return null;
        } catch (Exception e) {
            return e.getMessage() == null ? e.toString() : e.getMessage();
        }
    }

    /** Duplicate ids + dangling @id/ references inside one file. */
    public static java.util.List<String> validateIds(String xml) {
        java.util.List<String> problems = new java.util.ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(
                    xml.getBytes(StandardCharsets.UTF_8)));
            Set<String> ids = new HashSet<>();
            Set<String> duplicates = new HashSet<>();
            Set<String> references = new HashSet<>();
            collect(document.getDocumentElement(), ids, duplicates, references);
            for (String duplicate : duplicates) {
                problems.add("duplicate android:id '" + duplicate + "'");
            }
            for (String reference : references) {
                if (!ids.contains(reference)) {
                    problems.add("unresolved @id/" + reference);
                }
            }
        } catch (Exception e) {
            problems.add("unparseable XML: "
                    + (e.getMessage() == null ? e.toString() : e.getMessage()));
        }
        return problems;
    }

    private static void collect(Element element, Set<String> ids,
                                Set<String> duplicates, Set<String> references) {
        for (int i = 0; i < element.getAttributes().getLength(); i++) {
            Node attr = element.getAttributes().item(i);
            String value = attr.getNodeValue();
            if (value == null) {
                continue;
            }
            if ("android:id".equals(attr.getNodeName())
                    && value.startsWith("@+id/")) {
                String id = value.substring("@+id/".length());
                if (!ids.add(id)) {
                    duplicates.add(id);
                }
            } else if (value.startsWith("@id/")) {
                references.add(value.substring("@id/".length()));
            }
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element) {
                collect((Element) child, ids, duplicates, references);
            }
        }
    }
}
