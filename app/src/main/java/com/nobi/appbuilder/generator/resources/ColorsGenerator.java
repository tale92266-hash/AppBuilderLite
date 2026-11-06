package com.nobi.appbuilder.generator.resources;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.xml.XMLDocumentWriter;

import java.util.LinkedHashMap;
import java.util.Map;

/** res/values/colors.xml from the shared resource collector. */
public class ColorsGenerator {

    private final GenerationContext context;

    public ColorsGenerator(GenerationContext context) {
        this.context = context;
    }

    public String generate() {
        Map<String, String> colors = context.getResources().colors;
        XMLDocumentWriter writer = new XMLDocumentWriter();
        writer.startElement("resources");
        for (Map.Entry<String, String> entry : colors.entrySet()) {
            Map<String, String> attrs = new LinkedHashMap<>();
            attrs.put("name", entry.getValue());
            writer.textElement("color", attrs, entry.getKey());
        }
        writer.endElement();
        return writer.build();
    }
}
