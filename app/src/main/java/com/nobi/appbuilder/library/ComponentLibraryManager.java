package com.nobi.appbuilder.library;

import android.content.Context;

import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.library.custom.CustomComponentDefinition;
import com.nobi.appbuilder.library.custom.CustomComponentInstantiator;
import com.nobi.appbuilder.library.custom.CustomComponentRepository;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade for the component library: built-in catalog (17 registered
 * types + variants), custom components, favorites, recent use, and
 * Add to Canvas. Anything without working canvas + preview + XML
 * mappings is listed as unsupported, never added.
 */
public class ComponentLibraryManager {

    private final Context appContext;
    private final DatabaseHelper dbHelper;
    private final ComponentLibraryRepository repository;
    private final ComponentUsageTracker usageTracker;
    private final CustomComponentRepository customRepository;

    public ComponentLibraryManager(Context context, DatabaseHelper dbHelper) {
        this.appContext = context.getApplicationContext();
        this.dbHelper = dbHelper;
        this.repository = new ComponentLibraryRepository(dbHelper);
        this.usageTracker = new ComponentUsageTracker(dbHelper);
        this.customRepository = new CustomComponentRepository(dbHelper);
    }

    public ComponentLibraryRepository getRepository() {
        return repository;
    }

    public ComponentUsageTracker getUsageTracker() {
        return usageTracker;
    }

    public CustomComponentRepository getCustomRepository() {
        return customRepository;
    }

    /** Full built-in catalog: base types, variants, unsupported notes. */
    public static List<ComponentLibraryItem> builtInCatalog() {
        List<ComponentLibraryItem> items = new ArrayList<>();
        Map<String, String> props;

        items.add(ComponentLibraryItem.builtIn("LinearLayout", null,
                "Linear Layout", ComponentCategory.LAYOUTS,
                "A container that stacks children vertically or horizontally.",
                null));
        props = new LinkedHashMap<>();
        props.put("orientation", "horizontal");
        items.add(ComponentLibraryItem.builtIn("LinearLayout", "horizontal",
                "Nested Container", ComponentCategory.CONTAINERS,
                "A horizontal LinearLayout for nesting inside another layout.",
                props));
        items.add(ComponentLibraryItem.builtIn("FrameLayout", null,
                "Frame Layout", ComponentCategory.LAYOUTS,
                "A container that layers children on top of each other.",
                null));
        items.add(ComponentLibraryItem.builtIn("ConstraintLayout", null,
                "Constraint Layout", ComponentCategory.LAYOUTS,
                "A flat container that positions children with constraints.",
                null));
        items.add(ComponentLibraryItem.builtIn("ScrollView", null,
                "Scroll View", ComponentCategory.LAYOUTS,
                "A scrollable container for content taller than the screen.",
                null));
        items.add(ComponentLibraryItem.builtIn("MaterialCardView", null,
                "Card Container", ComponentCategory.CONTAINERS,
                "A rounded card container with elevation.",
                null));

        items.add(ComponentLibraryItem.builtIn("TextView", null,
                "Text", ComponentCategory.TEXT,
                "A read-only text label.", null));
        items.add(variant("TextView", "title", "Text Title", "A large heading.",
                "text", "Title", "textSize", "24"));
        items.add(variant("TextView", "subtitle", "Text Subtitle",
                "A medium sub-heading.", "text", "Subtitle", "textSize", "18"));
        items.add(variant("TextView", "paragraph", "Paragraph",
                "Body paragraph text.", "text", "Paragraph text", "textSize", "14"));
        items.add(variant("TextView", "label", "Label",
                "A small field label.", "text", "Label", "textSize", "12"));
        items.add(variant("TextView", "caption", "Caption",
                "Small grey helper text.", "text", "Caption", "textSize", "12",
                "textColor", "#6B7280"));

        items.add(ComponentLibraryItem.builtIn("Button", null,
                "Button", ComponentCategory.BUTTONS,
                "A Material-style button.", null));
        items.add(variant("Button", "text", "Text Button",
                "A borderless button with coloured text.",
                "text", "Text button", "backgroundColor", "#00000000",
                "textColor", "#0B4499"));
        items.add(variant("Button", "round", "Floating Action Button",
                "A round action button.",
                "text", "+", "cornerRadius", "28"));
        items.add(variant("Button", "icon", "Icon Button",
                "A small round button, e.g. for an icon.",
                "text", "★", "cornerRadius", "24"));

        items.add(ComponentLibraryItem.builtIn("EditText", null,
                "Text Field", ComponentCategory.INPUTS,
                "An editable text input.", null));
        items.add(variant("EditText", "password", "Password Field",
                "A password input with hidden text.",
                "inputType", "password", "hint", "Password"));
        items.add(variant("EditText", "search", "Search Field",
                "A search input.", "inputType", "text", "hint", "Search"));
        items.add(variant("EditText", "multiline", "Multiline Text Input",
                "A taller input for several lines of text.",
                "inputType", "multiline", "hint", "Write here"));
        items.add(variant("EditText", "number", "Number Input",
                "A numeric-only input.", "inputType", "number", "hint", "0"));

        items.add(ComponentLibraryItem.builtIn("ImageView", null,
                "Image", ComponentCategory.IMAGES,
                "Displays an image.", null));
        items.add(variant("ImageView", "circular", "Circular Image",
                "A round image, e.g. a profile photo.",
                "cornerRadius", "999", "backgroundColor", "#E8EEF7"));
        items.add(variant("ImageView", "background", "Background Image",
                "An image that fills its area, cropped to fit.",
                "scaleType", "centerCrop", "backgroundColor", "#E8EEF7"));
        items.add(variant("ImageView", "icon", "Icon Image",
                "A small image shown fully inside its bounds.",
                "scaleType", "centerInside", "backgroundColor", "#E8EEF7"));

        items.add(ComponentLibraryItem.builtIn("CheckBox", null,
                "Check Box", ComponentCategory.SELECTION,
                "A toggleable check box with a label.", null));
        items.add(ComponentLibraryItem.builtIn("Switch", null,
                "Switch", ComponentCategory.SELECTION,
                "An on/off switch.", null));
        items.add(ComponentLibraryItem.builtIn("RadioButton", null,
                "Radio Button", ComponentCategory.SELECTION,
                "A single-choice option.", null));
        items.add(ComponentLibraryItem.builtIn("Spinner", null,
                "Spinner", ComponentCategory.SELECTION,
                "A dropdown selector.", null));
        items.add(ComponentLibraryItem.builtIn("RatingBar", null,
                "Rating Bar", ComponentCategory.INDICATORS,
                "Star rating input.", null));
        items.add(ComponentLibraryItem.builtIn("SeekBar", null,
                "Seek Bar", ComponentCategory.INDICATORS,
                "A draggable slider.", null));
        items.add(ComponentLibraryItem.builtIn("ProgressBar", null,
                "Progress Bar", ComponentCategory.INDICATORS,
                "Shows progress or a loading spinner.", null));
        items.add(ComponentLibraryItem.builtIn("Divider", null,
                "Divider", ComponentCategory.INDICATORS,
                "A thin horizontal rule that separates content.", null));

        items.add(ComponentLibraryItem.unsupported("WebView", "Web View",
                ComponentCategory.DISPLAY,
                "Shows a web page inside the app.",
                "Needs WebView runtime and INTERNET permission handling — not supported yet."));
        items.add(ComponentLibraryItem.unsupported("Toolbar", "Toolbar",
                ComponentCategory.LAYOUTS,
                "An app bar with title and actions.",
                "Toolbar generation is not supported yet — use the editor toolbar instead."));
        items.add(ComponentLibraryItem.unsupported("BottomNavigation",
                "Bottom Navigation", ComponentCategory.CONTAINERS,
                "Bottom tab navigation.",
                "Needs a navigation graph and menu resources — not supported yet."));
        items.add(ComponentLibraryItem.unsupported("TabLayout", "Tab Layout",
                ComponentCategory.CONTAINERS,
                "Tab navigation.",
                "Needs ViewPager2 and a tab adapter — not supported yet."));
        items.add(ComponentLibraryItem.unsupported("RecyclerView",
                "Recycler View", ComponentCategory.CONTAINERS,
                "A scrolling list of repeated items.",
                "Needs an adapter and an item layout — not supported yet."));
        items.add(ComponentLibraryItem.unsupported("HorizontalScrollView",
                "Horizontal Scroll View", ComponentCategory.LAYOUTS,
                "A horizontally scrollable container.",
                "Horizontal scrolling is not supported yet — use ScrollView for vertical content."));
        items.add(ComponentLibraryItem.unsupported("RelativeLayout",
                "Relative Layout", ComponentCategory.LAYOUTS,
                "Positions children relative to each other.",
                "ConstraintLayout covers the same layouts — RelativeLayout is not supported yet."));
        return items;
    }

    private static ComponentLibraryItem variant(String baseType, String variant,
                                                String displayName,
                                                String description,
                                                String... keyValues) {
        Map<String, String> props = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            props.put(keyValues[i], keyValues[i + 1]);
        }
        ComponentCategory category = ComponentCategory.TEXT;
        if ("Button".equals(baseType)) {
            category = ComponentCategory.BUTTONS;
        } else if ("EditText".equals(baseType)) {
            category = ComponentCategory.INPUTS;
        } else if ("ImageView".equals(baseType)) {
            category = ComponentCategory.IMAGES;
        } else if ("LinearLayout".equals(baseType)) {
            category = ComponentCategory.CONTAINERS;
        }
        return ComponentLibraryItem.builtIn(baseType, variant, displayName,
                category, description, props);
    }

    /** Seed the DB catalog for a project on first open. */
    public void seedIfEmpty(String projectId) {
        if (!repository.getAll(projectId).isEmpty()) {
            return;
        }
        for (ComponentLibraryItem item : builtInCatalog()) {
            repository.upsert(item, projectId);
        }
    }

    /** Catalog with favorites + recent-use attached. */
    public List<ComponentLibraryItem> getCatalog(String projectId) {
        seedIfEmpty(projectId);
        List<ComponentLibraryItem> items = repository.getAll(projectId);
        for (ComponentLibraryItem item : items) {
            item.setFavorite(repository.isFavorite(projectId, item.getId()));
            item.setLastUsedAt(usageTracker.getLastUsedAt(
                    projectId, item.getBaseType()));
        }
        for (CustomComponentDefinition custom
                : customRepository.getByProject(projectId)) {
            ComponentLibraryItem item = new ComponentLibraryItem(
                    "custom:" + custom.getId(), "custom", null,
                    custom.getName(), ComponentCategory.CUSTOM,
                    custom.getDescription(), null, true, null,
                    custom.getId());
            items.add(item);
        }
        return items;
    }

    /**
     * Adds an item to a screen's root container and saves the
     * document. Returns the created model, or null when the item
     * is unsupported or the screen cannot be loaded.
     */
    public ComponentModel addToCanvas(String projectId, String screenId,
                                      ComponentLibraryItem item,
                                      int xDp, int yDp) {
        if (item == null || !item.isSupported()) {
            return null;
        }
        DocumentRepository repo = new DocumentRepository(dbHelper);
        IRDocument document = repo.loadDocument(projectId);
        if (document == null) {
            return null;
        }
        ScreenModel screen = document.getScreen(screenId);
        if (screen == null) {
            return null;
        }
        ComponentModel created;
        if (item.isCustom()) {
            CustomComponentDefinition definition =
                    customRepository.getById(item.getCustomComponentId());
            if (definition == null) {
                return null;
            }
            created = new CustomComponentInstantiator()
                    .instantiate(definition, null);
        } else {
            if (!ComponentRegistry.contains(item.getBaseType())) {
                return null;
            }
            created = ComponentFactory.create(item.getBaseType(), xDp, yDp);
            for (Map.Entry<String, String> entry
                    : item.getDefaultProperties().entrySet()) {
                created.setProperty(entry.getKey(), entry.getValue());
            }
        }
        if (created == null) {
            return null;
        }
        ComponentModel root = screen.getRootComponent();
        if (root == null) {
            root = ComponentFactory.createRootContainer();
            screen.setRootComponent(root);
        }
        root.addChild(created);
        if (!repo.saveDocument(document)) {
            return null;
        }
        usageTracker.recordUse(projectId, item.getBaseType());
        return created;
    }
}
