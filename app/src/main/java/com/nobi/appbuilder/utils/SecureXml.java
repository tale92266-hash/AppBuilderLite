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
 *
 * Two more Android traps live here:
 * - setXIncludeAware() is NEVER called: Android's factory throws
 *   UnsupportedOperationException ("This parser does not support
 *   specification ...") for it, always, even with false. That was
 *   the second on-device export failure (fix1).
 * - setExpandEntityReferences(false) is a plain field setter on
 *   Android, but it is still applied defensively (any failure is
 *   ignored) so no future platform quirk can break parsing again.
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
        // setXIncludeAware() is deliberately NOT called (see class doc).
        setExpandEntityReferences(factory, false);
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
        } catch (Exception ignored) {
            // Unsupported on this parser (e.g. Android's built-in one).
        }
    }

    private static void setExpandEntityReferences(
            DocumentBuilderFactory factory, boolean value) {
        try {
            factory.setExpandEntityReferences(value);
        } catch (Exception ignored) {
            // Plain setter on Android; guarded so a platform quirk
            // can never break parsing.
        }
    }
}
