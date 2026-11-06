package com.nobi.appbuilder.generator.resources;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.xml.XMLDocumentWriter;

import java.util.LinkedHashMap;
import java.util.Map;

/** res/values/dimens.xml from the shared resource collector. */
public class DimensionsGenerator {

    private final GenerationContext context;

    public DimensionsGenerator(GenerationContext context) {
        this.context = context;
    }

    public String generate() {
        Map<String, String> dimens = context.getResources().dimens;
        XMLDocumentWriter writer = new XMLDocumentWriter();
        writer.startElement("resources");
        for (Map.Entry<String, String> entry : dimens.entrySet()) {
            Map<String, String> attrs = new LinkedHashMap<>();
            attrs.put("name", entry.getValue());
            writer.textElement("dimen", attrs, entry.getKey());
        }
        writer.endElement();
        return writer.build();
    }
}
