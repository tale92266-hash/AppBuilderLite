package com.nobi.appbuilder.sync.parser;

import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.sync.diff.DiffResult;
import com.nobi.appbuilder.sync.diff.ScreenDiffEngine;
import com.nobi.appbuilder.sync.mapper.IRToXMLMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Round-trip check: JSON IR → XML → parsed XML → IR tree, then
 * compare with the original. Reports every mismatch instead of
 * claiming a perfect round-trip for unsupported constructs.
 */
public class XMLRoundTripValidator {

    private final IRToXMLMapper irToXmlMapper;

    public XMLRoundTripValidator(IRToXMLMapper irToXmlMapper) {
        this.irToXmlMapper = irToXmlMapper;
    }

    /** Mismatches; empty means the round-trip is faithful. */
    public List<String> validate(IRDocument document, ScreenModel screen) {
        List<String> mismatches = new ArrayList<>();
        String xml = irToXmlMapper.generateXml(document, screen);
        if (xml == null) {
            mismatches.add("XML generation failed for screen "
                    + screen.getId());
            return mismatches;
        }
        XMLParseResult parsed = new XMLLayoutParser().parseText(xml);
        if (!parsed.isSuccess()) {
            for (XMLParseError error : parsed.getErrors()) {
                mismatches.add("Generated XML does not parse: "
                        + error.toString());
            }
            return mismatches;
        }
        XMLHierarchyParser hierarchyParser = new XMLHierarchyParser();
        ComponentModel roundTripped = hierarchyParser.parseTree(
                parsed.getRoot(), null);
        if (roundTripped == null) {
            mismatches.add("Round-trip produced no root component");
            return mismatches;
        }
        ComponentModel original = screen.getRootComponent();
        if (original == null) {
            mismatches.add("Screen has no root component");
            return mismatches;
        }
        DiffResult diff = new ScreenDiffEngine().diff(original,
                roundTripped);
        if (diff.hasChanges()) {
            mismatches.add(diff.getSummary());
        }
        mismatches.addAll(hierarchyParser.getUnsupported());
        return mismatches;
    }
}
