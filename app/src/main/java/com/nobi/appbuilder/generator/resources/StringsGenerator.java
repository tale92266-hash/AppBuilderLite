package com.nobi.appbuilder.generator.resources;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.xml.XMLDocumentWriter;

import java.util.LinkedHashMap;
import java.util.Map;

/** res/values/strings.xml: app_name plus every extracted string. */
public class StringsGenerator {

    private final GenerationContext context;

    public StringsGenerator(GenerationContext context) {
        this.context = context;
    }

    public String generate() {
        Map<String, String> strings = context.getResources().strings;
        XMLDocumentWriter writer = new XMLDocumentWriter();
        writer.startElement("resources");
        Map<String, String> appNameAttrs = new LinkedHashMap<>();
        appNameAttrs.put("name", "app_name");
        writer.textElement("string", appNameAttrs,
                context.getProject().getAppName());
        for (Map.Entry<String, String> entry : strings.entrySet()) {
            if ("app_name".equals(entry.getValue())) {
                continue;
            }
            Map<String, String> attrs = new LinkedHashMap<>();
            attrs.put("name", entry.getValue());
            writer.textElement("string", attrs, entry.getKey());
        }
        writer.endElement();
        return writer.build();
    }
}
