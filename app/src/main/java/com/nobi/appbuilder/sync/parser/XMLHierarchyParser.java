package com.nobi.appbuilder.sync.parser;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.sync.mapper.ComponentSyncMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a ComponentModel tree from a parsed XML tree. Components
 * are matched to existing IR components by android:id; unknown ids
 * become new components with the XML id as their component id.
 * Unsupported tags are skipped and reported, never converted into
 * generic components that would silently lose structure.
 */
public class XMLHierarchyParser {

    private final XMLComponentMapper componentMapper =
            new XMLComponentMapper();
    private final XMLPropertyExtractor extractor =
            new XMLPropertyExtractor();
    private final List<String> unsupported = new ArrayList<>();

    public ComponentModel parseTree(XMLParseResult.ParsedElement root,
                                    ComponentModel existingRoot) {
        unsupported.clear();
        return parseElement(root, existingRoot);
    }

    public List<String> getUnsupported() {
        return new ArrayList<>(unsupported);
    }

    private ComponentModel parseElement(
            XMLParseResult.ParsedElement element,
            ComponentModel existing) {
        String type = componentMapper.typeForTag(element.tag);
        if (type == null) {
            unsupported.add("Unsupported element <" + element.tag + ">");
            return null;
        }
        String androidId = element.attr("android:id");
        String componentId = androidId == null ? null
                : ComponentSyncMapper.componentIdFromAndroidId(androidId);
        ComponentModel model = existing;
        if (model == null || (componentId != null
                && !componentId.equals(model.getId()))) {
            model = new ComponentModel();
            model.setId(componentId == null
                    ? ComponentSyncMapper.newComponentId(type) : componentId);
            model.setType(type);
        } else if (!type.equals(model.getType())) {
            // The XML tag changed type (e.g. TextView -> Button).
            model.setType(type);
        }
        XMLPropertyExtractor.Extracted extracted = extractor.extract(element);
        for (String unknown : extracted.unknownAttrs.keySet()) {
            unsupported.add("Unsupported attribute " + unknown
                    + " on <" + element.tag + ">");
        }
        for (java.util.Map.Entry<String, String> entry
                : extracted.componentProps.entrySet()) {
            model.setProperty(entry.getKey(), entry.getValue());
        }
        applyLayout(model, extracted.layoutProps);
        List<ComponentModel> children = new ArrayList<>();
        for (XMLParseResult.ParsedElement child : element.children) {
            ComponentModel existingChild = findChildByAndroidId(model, child);
            ComponentModel parsed = parseElement(child, existingChild);
            if (parsed != null) {
                children.add(parsed);
            }
        }
        model.setChildren(children);
        return model;
    }

    private ComponentModel findChildByAndroidId(
            ComponentModel parent, XMLParseResult.ParsedElement child) {
        if (parent == null || parent.getChildren() == null) {
            return null;
        }
        String androidId = child.attr("android:id");
        if (androidId == null) {
            return null;
        }
        String componentId =
                ComponentSyncMapper.componentIdFromAndroidId(androidId);
        for (ComponentModel candidate : parent.getChildren()) {
            if (componentId.equals(candidate.getId())) {
                return candidate;
            }
        }
        return null;
    }

    private void applyLayout(ComponentModel model,
                             java.util.Map<String, String> layoutProps) {
        com.nobi.appbuilder.model.LayoutModel layout = model.getLayout();
        if (layout == null) {
            return;
        }
        for (java.util.Map.Entry<String, String> entry
                : layoutProps.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            try {
                switch (key) {
                    case "width":
                        layout.setWidth(value);
                        break;
                    case "height":
                        layout.setHeight(value);
                        break;
                    case "padding":
                        int padding = parseDimension(value);
                        layout.setPaddingLeft(padding);
                        layout.setPaddingTop(padding);
                        layout.setPaddingRight(padding);
                        layout.setPaddingBottom(padding);
                        break;
                    case "paddingLeft":
                        layout.setPaddingLeft(parseDimension(value));
                        break;
                    case "paddingTop":
                        layout.setPaddingTop(parseDimension(value));
                        break;
                    case "paddingRight":
                        layout.setPaddingRight(parseDimension(value));
                        break;
                    case "paddingBottom":
                        layout.setPaddingBottom(parseDimension(value));
                        break;
                    case "margin":
                        int margin = parseDimension(value);
                        layout.setMarginLeft(margin);
                        layout.setMarginTop(margin);
                        layout.setMarginRight(margin);
                        layout.setMarginBottom(margin);
                        break;
                    case "marginLeft":
                        layout.setMarginLeft(parseDimension(value));
                        break;
                    case "marginTop":
                        layout.setMarginTop(parseDimension(value));
                        break;
                    case "marginRight":
                        layout.setMarginRight(parseDimension(value));
                        break;
                    case "marginBottom":
                        layout.setMarginBottom(parseDimension(value));
                        break;
                    case "layoutGravity":
                        layout.setLayoutGravity(value);
                        break;
                    case "weight":
                        layout.setWeight(Float.parseFloat(value));
                        break;
                    default:
                        break;
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private int parseDimension(String value) {
        if (value == null) {
            return 0;
        }
        String digits = value.replace("dp", "").replace("sp", "")
                .replace("px", "").trim();
        try {
            return (int) Float.parseFloat(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
