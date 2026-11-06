package com.nobi.appbuilder.utils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Android-safe secure XML parsing.
 *
 * The textbook XXE-hardening snippet sets the Xerces-only feature
 * "http://apache.org/xml/features/disallow-doctype-decl".
 * Android's built-in parser does not recognise it, so
 * DocumentBuilderFactory throws ParserConfigurationException — and
 * every caller that reported that exception as a "parse error"
 * failed every document, including perfectly valid generated XML.
 * That is what killed Phase 8 export on device (0 files in 27 ms:
 * the layout validator rejected valid XML before a single file
 * was written). Every feature here is therefore set best-effort:
 * an unsupported one is skipped, never fatal.
 * setExpandEntityReferences(false) and setXIncludeAware(false) are
 * standard JAXP and always apply.
 */
public final class SecureXml {

    private SecureXml() {
    }

    public static DocumentBuilderFactory newFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        setFeature(factory,
                "http://apache.org/xml/features/disallow-doctype-decl",
                true);
        setFeature(factory,
                "http://xml.org/sax/features/external-general-entities",
                false);
        setFeature(factory,
                "http://xml.org/sax/features/external-parameter-entities",
                false);
        setFeature(factory,
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    public static DocumentBuilder newBuilder()
            throws ParserConfigurationException {
        return newFactory().newDocumentBuilder();
    }

    private static void setFeature(DocumentBuilderFactory factory,
                                   String name, boolean value) {
        try {
            factory.setFeature(name, value);
        } catch (ParserConfigurationException ignored) {
            // Unsupported on this parser (e.g. Android's built-in one).
        }
    }
}
