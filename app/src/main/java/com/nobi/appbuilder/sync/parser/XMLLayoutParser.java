package com.nobi.appbuilder.sync.parser;

import com.nobi.appbuilder.utils.SecureXml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.xml.parsers.DocumentBuilder;

/**
 * Parses a layout XML file into a small element tree. Tolerates
 * malformed XML by reporting errors instead of crashing — a broken
 * file must never take the visual editor down with it.
 */
public class XMLLayoutParser {

    public XMLParseResult parse(File file) {
        try {
            String text = new String(Files.readAllBytes(file.toPath()),
                    StandardCharsets.UTF_8);
            return parseText(text);
        } catch (Exception e) {
            XMLParseResult result = new XMLParseResult(null);
            result.addError(new XMLParseError(
                    "Could not read file: " + e.getMessage(), 0, 0));
            return result;
        }
    }

    public XMLParseResult parseText(String xml) {
        try {
            DocumentBuilder builder = SecureXml.newBuilder();
            Document document = builder.parse(new ByteArrayInputStream(
                    xml.getBytes(StandardCharsets.UTF_8)));
            Element root = document.getDocumentElement();
            if (root == null) {
                XMLParseResult result = new XMLParseResult(null);
                result.addError(new XMLParseError("Empty XML document",
                        0, 0));
                return result;
            }
            return new XMLParseResult(convert(root));
        } catch (Exception e) {
            XMLParseResult result = new XMLParseResult(null);
            int line = extractLine(e.getMessage());
            result.addError(new XMLParseError(
                    "Malformed XML: " + e.getMessage(), line, 0));
            return result;
        }
    }

    private XMLParseResult.ParsedElement convert(Element element) {
        XMLParseResult.ParsedElement out =
                new XMLParseResult.ParsedElement(element.getTagName());
        NamedNodeMap attrs = element.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Node attr = attrs.item(i);
            out.attributes.put(attr.getNodeName(), attr.getNodeValue());
        }
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                out.children.add(convert((Element) child));
            }
        }
        return out;
    }

    private int extractLine(String message) {
        if (message == null) {
            return 0;
        }
        java.util.regex.Matcher matcher =
                java.util.regex.Pattern.compile("lineNumber: (\\d+)")
                        .matcher(message);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }
}
