package com.nobi.appbuilder.sync.parser;

import com.nobi.appbuilder.utils.SecureXml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;

/**
 * Resolves @string/@color/@dimen/@drawable references against the
 * generated values files in a workspace. Returns null when a
 * reference cannot be resolved — the caller keeps the raw reference
 * instead of inventing a value.
 */
public class XMLResourceResolver {

    private final Map<String, String> strings = new HashMap<>();
    private final Map<String, String> colors = new HashMap<>();
    private final Map<String, String> dimens = new HashMap<>();

    public XMLResourceResolver(File workspaceRoot) {
        File valuesDir = new File(workspaceRoot,
                "app/src/main/res/values");
        load(valuesDir, "strings.xml", "string", strings);
        load(valuesDir, "colors.xml", "color", colors);
        load(valuesDir, "dimens.xml", "dimen", dimens);
    }

    private void load(File valuesDir, String fileName, String tag,
                      Map<String, String> out) {
        File file = new File(valuesDir, fileName);
        if (!file.exists()) {
            return;
        }
        try {
            DocumentBuilder builder = SecureXml.newBuilder();
            Document document = builder.parse(file);
            NodeList nodes = document.getElementsByTagName(tag);
            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                String name = element.getAttribute("name");
                if (name != null && !name.isEmpty()) {
                    out.put(name, element.getTextContent());
                }
            }
        } catch (Exception ignored) {
        }
    }

    /** Resolves "@string/app_name" etc. Returns null if unknown. */
    public String resolve(String reference) {
        if (reference == null || !reference.startsWith("@")) {
            return reference;
        }
        String[] parts = reference.substring(1).split("/", 2);
        if (parts.length != 2) {
            return null;
        }
        switch (parts[0]) {
            case "string":
                return strings.get(parts[1]);
            case "color":
                return colors.get(parts[1]);
            case "dimen":
                return dimens.get(parts[1]);
            default:
                return null;
        }
    }

    public boolean isResolvable(String reference) {
        return resolve(reference) != null;
    }
}
