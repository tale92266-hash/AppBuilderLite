package com.nobi.appbuilder.sync.mapper;

import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.xml.XMLGenerator;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ScreenModel;

/**
 * Visual-to-code direction: JSON IR -> layout XML. Delegates to the
 * Phase 7 XMLGenerator so generated XML is byte-identical to what
 * the code generator produces. Only the changed screen's file is
 * regenerated, never the whole project.
 */
public class IRToXMLMapper {

    private GenerationContext buildContext(IRDocument document) {
        return new GenerationContext(document, document.getProject(),
                document.getProject().getPackageName(),
                document.getProject().getMinSdk(), 35, 1, "1.0",
                (AssetRepository) null);
    }

    /** Layout XML for one screen, or null when generation fails. */
    public String generateXml(IRDocument document, ScreenModel screen) {
        GenerationContext context = buildContext(document);
        return new XMLGenerator(context).generateLayout(screen);
    }

    /** Workspace-relative path of a screen's layout file. */
    public String layoutPathFor(IRDocument document, ScreenModel screen) {
        GenerationContext context = buildContext(document);
        return new XMLGenerator(context).layoutPathFor(screen);
    }
}
