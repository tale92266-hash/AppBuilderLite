package com.nobi.appbuilder.library.templates;

import android.content.Context;

import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ScreenRepository;
import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.ir.IRSerializer;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.repository.DocumentRepository;
import com.nobi.appbuilder.screens.ScreenManager;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade for screen templates: six original starter templates,
 * personal templates in SQLite, import/export, and applying a
 * template as a new screen (fresh component ids every time).
 */
public class TemplateManager {

    private final Context appContext;
    private final DatabaseHelper dbHelper;
    private final TemplateRepository repository;

    public TemplateManager(Context context, DatabaseHelper dbHelper) {
        this.appContext = context.getApplicationContext();
        this.dbHelper = dbHelper;
        this.repository = new TemplateRepository(dbHelper);
    }

    public TemplateRepository getRepository() {
        return repository;
    }

    public static List<TemplateDefinition> builtInTemplates() {
        List<TemplateDefinition> templates = new ArrayList<>();
        templates.add(builtIn("builtin-login", "Login Screen",
                TemplateCategory.AUTHENTICATION,
                "Email + password login with a log-in button.",
                "Login", loginRoot()));
        templates.add(builtIn("builtin-dashboard", "Dashboard Screen",
                TemplateCategory.DASHBOARD,
                "A heading plus a summary card.",
                "Dashboard", dashboardRoot()));
        templates.add(builtIn("builtin-profile", "Profile Screen",
                TemplateCategory.PROFILE,
                "Profile photo, name, bio and an edit button.",
                "Profile", profileRoot()));
        templates.add(builtIn("builtin-settings", "Settings Screen",
                TemplateCategory.SETTINGS,
                "Toggles for notifications and dark mode.",
                "Settings", settingsRoot()));
        templates.add(builtIn("builtin-contact", "Contact Screen",
                TemplateCategory.CONTACT,
                "A simple contact form.",
                "Contact", contactRoot()));
        templates.add(builtIn("builtin-empty", "Empty Screen",
                TemplateCategory.EMPTY,
                "A blank screen to start from.",
                "Empty", ComponentFactory.createRootContainer()));
        return templates;
    }

    private static TemplateDefinition builtIn(String id, String name,
                                              TemplateCategory category,
                                              String description,
                                              String screenName,
                                              ComponentModel root) {
        TemplateDefinition template = new TemplateDefinition();
        template.setId(id);
        template.setName(name);
        template.setCategory(category);
        template.setDescription(description);
        template.setVersion(1);
        template.setScreenName(screenName);
        template.setScreenJson(IRSerializer.serializeComponent(root));
        template.setBuiltIn(true);
        template.setCreatedAt(0);
        return template;
    }

    public List<TemplateDefinition> getAll(String projectId) {
        List<TemplateDefinition> out = new ArrayList<>(builtInTemplates());
        out.addAll(repository.getByProject(projectId));
        return out;
    }

    /** Creates a new screen from a template. Returns null on failure. */
    public ScreenModel createScreenFromTemplate(String projectId,
                                                TemplateDefinition template,
                                                List<String> errors) {
        List<String> problems = TemplateValidator.validate(template);
        if (!problems.isEmpty()) {
            errors.addAll(problems);
            return null;
        }
        ComponentModel saved;
        try {
            saved = IRDeserializer.deserializeComponent(
                    template.getScreenJson());
        } catch (Exception e) {
            errors.add("Template screen could not be read: " + e.getMessage());
            return null;
        }
        ScreenManager screenManager = new ScreenManager(
                new ScreenRepository(dbHelper));
        String name = template.getScreenName();
        List<ScreenModel> existing = screenManager.getScreens(projectId);
        int suffix = 2;
        String candidate = name;
        while (nameTaken(existing, candidate)) {
            candidate = name + " " + suffix;
            suffix++;
        }
        ScreenModel screen = screenManager.createScreen(projectId, candidate,
                ScreenModel.TYPE_ACTIVITY, ScreenModel.ORIENTATION_PORTRAIT);
        if (screen == null) {
            errors.add("Could not create the screen");
            return null;
        }
        DocumentRepository repo = new DocumentRepository(dbHelper);
        IRDocument document = repo.loadDocument(projectId);
        if (document == null) {
            errors.add("Could not load the project");
            return null;
        }
        ScreenModel fresh = document.getScreen(screen.getId());
        if (fresh == null) {
            errors.add("Created screen could not be loaded");
            return null;
        }
        fresh.setRootComponent(ComponentFactory.cloneTree(saved));
        if (!repo.saveDocument(document)) {
            errors.add("Could not save the new screen");
            return null;
        }
        return fresh;
    }

    /** Saves the current screen as a personal template. */
    public TemplateDefinition createFromScreen(String projectId,
                                               String screenId,
                                               String name,
                                               TemplateCategory category,
                                               List<String> errors) {
        DocumentRepository repo = new DocumentRepository(dbHelper);
        IRDocument document = repo.loadDocument(projectId);
        if (document == null) {
            errors.add("Could not load the project");
            return null;
        }
        ScreenModel screen = document.getScreen(screenId);
        if (screen == null || screen.getRootComponent() == null) {
            errors.add("Screen has no content to save as a template");
            return null;
        }
        TemplateDefinition template = new TemplateDefinition();
        template.setId(IDGenerator.newId());
        template.setProjectId(projectId);
        template.setName(name);
        template.setCategory(category == null ? TemplateCategory.EMPTY : category);
        template.setDescription("Saved from screen " + screen.getName());
        template.setVersion(1);
        template.setScreenName(screen.getName());
        template.setScreenJson(IRSerializer.serializeComponent(
                screen.getRootComponent()));
        template.setBuiltIn(false);
        template.setCreatedAt(System.currentTimeMillis());
        List<String> problems = TemplateValidator.validate(template);
        if (!problems.isEmpty()) {
            errors.addAll(problems);
            return null;
        }
        if (!repository.save(template)) {
            errors.add("Could not save the template");
            return null;
        }
        return template;
    }

    public boolean delete(String templateId) {
        return repository.delete(templateId);
    }

    private boolean nameTaken(List<ScreenModel> existing, String name) {
        for (ScreenModel screen : existing) {
            if (screen.getName() != null
                    && screen.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    // ---------------------------------------------------------- builders

    private static ComponentModel text(String value, String size) {
        ComponentModel model = ComponentFactory.create("TextView");
        model.setProperty("text", value);
        model.setProperty("textSize", size);
        model.getLayout().setWidth("match_parent");
        return model;
    }

    private static ComponentModel field(String hint, String inputType) {
        ComponentModel model = ComponentFactory.create("EditText");
        model.setProperty("hint", hint);
        model.setProperty("inputType", inputType);
        model.getLayout().setWidth("match_parent");
        return model;
    }

    private static ComponentModel button(String label) {
        ComponentModel model = ComponentFactory.create("Button");
        model.setProperty("text", label);
        model.getLayout().setWidth("match_parent");
        return model;
    }

    private static ComponentModel loginRoot() {
        ComponentModel root = ComponentFactory.createRootContainer();
        root.addChild(text("Welcome back", "24"));
        root.addChild(field("Email", "email"));
        root.addChild(field("Password", "password"));
        root.addChild(button("Log in"));
        ComponentModel forgot = text("Forgot password?", "12");
        forgot.setProperty("textColor", "#0B4499");
        root.addChild(forgot);
        return root;
    }

    private static ComponentModel dashboardRoot() {
        ComponentModel root = ComponentFactory.createRootContainer();
        root.addChild(text("Dashboard", "24"));
        ComponentModel card = ComponentFactory.create("MaterialCardView");
        card.getLayout().setWidth("match_parent");
        card.addChild(text("Total sales", "16"));
        ComponentModel amount = text("$12,400", "24");
        card.addChild(amount);
        root.addChild(card);
        return root;
    }

    private static ComponentModel profileRoot() {
        ComponentModel root = ComponentFactory.createRootContainer();
        ComponentModel image = ComponentFactory.create("ImageView");
        image.setProperty("cornerRadius", "999");
        image.setProperty("backgroundColor", "#E8EEF7");
        root.addChild(image);
        root.addChild(text("Your Name", "20"));
        root.addChild(text("A short bio about you.", "14"));
        root.addChild(button("Edit profile"));
        return root;
    }

    private static ComponentModel settingsRoot() {
        ComponentModel root = ComponentFactory.createRootContainer();
        root.addChild(text("Settings", "24"));
        ComponentModel notifications = ComponentFactory.create("CheckBox");
        notifications.setProperty("text", "Notifications");
        root.addChild(notifications);
        ComponentModel dark = ComponentFactory.create("Switch");
        dark.setProperty("text", "Dark mode");
        root.addChild(dark);
        root.addChild(ComponentFactory.create("Divider"));
        return root;
    }

    private static ComponentModel contactRoot() {
        ComponentModel root = ComponentFactory.createRootContainer();
        root.addChild(text("Contact us", "24"));
        root.addChild(field("Your name", "text"));
        root.addChild(field("Email", "email"));
        root.addChild(field("Your message", "multiline"));
        root.addChild(button("Send"));
        return root;
    }
}
