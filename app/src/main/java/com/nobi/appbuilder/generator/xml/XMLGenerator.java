package com.nobi.appbuilder.generator.xml;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;

/**
 * Generates one res/layout/activity_*.xml per screen. The root
 * element carries the android (and, when a ConstraintLayout is
 * involved, the app) namespace; every component gets a unique id;
 * the result is parsed back by XMLValidator before it is accepted.
 */
public class XMLGenerator {

    private final GenerationContext context;

    public XMLGenerator(GenerationContext context) {
        this.context = context;
    }

    /** Relative path of a screen's layout file. */
    public String layoutPathFor(ScreenModel screen) {
        return "app/src/main/res/layout/" + context.layoutFileFor(screen.getId())
                + ".xml";
    }

    /** Generates (and validates) the layout XML for one screen. */
    public String generateLayout(ScreenModel screen) {
        String path = layoutPathFor(screen);
        ComponentModel root = screen.getRootComponent();
        if (root == null) {
            context.addError("xml", path, screen.getId(),
                    "screen has no root component");
            return null;
        }
        // Namespaces live on the root element; the writer emits them
        // as attributes because it has no namespace API of its own.
        LayoutXMLGenerator generator = new LayoutXMLGenerator(context);
        // Build the tree, then inject the xmlns declarations into the
        // root element's opening tag.
        String xml = buildWithNamespaces(generator, root);
        String parseError = XMLValidator.parse(xml);
        if (parseError != null) {
            context.addError("xml", path, screen.getId(),
                    "generated XML does not parse: " + parseError);
            return null;
        }
        for (String problem : XMLValidator.validateIds(xml)) {
            context.addError("xml", path, screen.getId(), problem);
        }
        context.recordLayout(context.layoutFileFor(screen.getId()));
        return xml;
    }

    private String buildWithNamespaces(LayoutXMLGenerator generator,
                                       ComponentModel root) {
        // The writer writes the XML declaration in its constructor.
        // We render the tree into a fresh writer, then inject the
        // namespace declarations into the root element's opening tag.
        XMLDocumentWriter tree = new XMLDocumentWriter();
        generator.writeComponent(tree, root, null);
        String built = tree.build();
        // The first element after the declaration is the root tag.
        int declEnd = built.indexOf("?>\n");
        String head = built.substring(0, declEnd + 3);
        String body = built.substring(declEnd + 3);
        boolean needsAppNs = body.contains("app:layout_constraint")
                || body.contains("app:card");
        String namespaces = " xmlns:android=\"http://schemas.android.com/apk/res/android\"";
        if (needsAppNs) {
            namespaces += " xmlns:app=\"http://schemas.android.com/apk/res-auto\"";
        }
        // Insert the namespaces right after the root tag name.
        int tagEnd = body.indexOf('>');
        int spaceAt = body.indexOf(' ');
        int newlineAt = body.indexOf('\n');
        int insertAt;
        if (spaceAt > 0 && (newlineAt < 0 || spaceAt < newlineAt)) {
            insertAt = spaceAt;
        } else if (tagEnd > 0 && body.charAt(tagEnd - 1) == '/') {
            // Self-closing root (an empty container has no children):
            // the namespaces go BEFORE the "/>", not after it —
            // inserting after the "/" produces
            // "<LinearLayout ... / xmlns:android=...>" which does not
            // parse at all.
            insertAt = tagEnd - 1;
        } else {
            insertAt = tagEnd;
        }
        // When attributes follow on later lines, the tag name ends at
        // the first space or newline; insert namespaces there.
        String withNs = body.substring(0, insertAt) + namespaces
                + body.substring(insertAt);
        return head + withNs;
    }
}
