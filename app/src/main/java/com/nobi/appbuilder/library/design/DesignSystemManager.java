package com.nobi.appbuilder.library.design;

import android.content.Context;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.repository.DocumentRepository;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade for the design system: default colour / typography /
 * spacing / shape / elevation tokens, custom token values, and
 * themes. Applying a theme updates the JSON IR theme metadata
 * only — component-level explicit colours are never overwritten.
 */
public class DesignSystemManager {

    private final Context appContext;
    private final DatabaseHelper dbHelper;
    private final DesignTokenRepository tokenRepository;
    private final ThemeRepository themeRepository;

    public DesignSystemManager(Context context, DatabaseHelper dbHelper) {
        this.appContext = context.getApplicationContext();
        this.dbHelper = dbHelper;
        this.tokenRepository = new DesignTokenRepository(dbHelper);
        this.themeRepository = new ThemeRepository(dbHelper);
    }

    public DesignTokenRepository getTokenRepository() {
        return tokenRepository;
    }

    public ThemeRepository getThemeRepository() {
        return themeRepository;
    }

    public static List<ColorToken> defaultColors() {
        List<ColorToken> colors = new ArrayList<>();
        colors.add(new ColorToken("Primary", "#0B4499"));
        colors.add(new ColorToken("Primary variant", "#082F6B"));
        colors.add(new ColorToken("Secondary", "#0B4499"));
        colors.add(new ColorToken("Accent", "#0B4499"));
        colors.add(new ColorToken("Background", "#FFFFFF"));
        colors.add(new ColorToken("Surface", "#F5F5F5"));
        colors.add(new ColorToken("Error", "#B3261E"));
        colors.add(new ColorToken("Success", "#2E7D32"));
        colors.add(new ColorToken("Warning", "#F59E0B"));
        colors.add(new ColorToken("Text primary", "#111827"));
        colors.add(new ColorToken("Text secondary", "#6B7280"));
        return colors;
    }

    public static List<TypographyToken> defaultTypography() {
        List<TypographyToken> type = new ArrayList<>();
        type.add(new TypographyToken("Display", 32, 700, 40, 0, ""));
        type.add(new TypographyToken("Headline", 24, 700, 32, 0, ""));
        type.add(new TypographyToken("Title", 20, 600, 28, 0, ""));
        type.add(new TypographyToken("Subtitle", 18, 600, 24, 0, ""));
        type.add(new TypographyToken("Body", 16, 400, 22, 0, ""));
        type.add(new TypographyToken("Label", 14, 500, 20, 0, ""));
        type.add(new TypographyToken("Caption", 12, 400, 16, 0, ""));
        return type;
    }

    public static List<SpacingToken> defaultSpacing() {
        List<SpacingToken> spacing = new ArrayList<>();
        int[] values = {4, 8, 12, 16, 20, 24, 32, 40, 48};
        for (int value : values) {
            spacing.add(new SpacingToken("Spacing " + value, value));
        }
        return spacing;
    }

    public static List<ShapeToken> defaultShapes() {
        List<ShapeToken> shapes = new ArrayList<>();
        shapes.add(new ShapeToken("Small", 4));
        shapes.add(new ShapeToken("Medium", 8));
        shapes.add(new ShapeToken("Large", 16));
        shapes.add(new ShapeToken("Full rounded", 999));
        return shapes;
    }

    public static List<ElevationToken> defaultElevations() {
        List<ElevationToken> elevations = new ArrayList<>();
        elevations.add(new ElevationToken("None", 0));
        elevations.add(new ElevationToken("Low", 2));
        elevations.add(new ElevationToken("Medium", 4));
        elevations.add(new ElevationToken("High", 8));
        return elevations;
    }

    /** Seeds the default tokens for a project on first use. */
    public void seedIfEmpty(String projectId) {
        if (!tokenRepository.getByProjectAndKind(projectId,
                DesignToken.KIND_COLOR).isEmpty()) {
            return;
        }
        for (ColorToken color : defaultColors()) {
            tokenRepository.save(color.toToken(IDGenerator.newId(), projectId));
        }
        for (TypographyToken type : defaultTypography()) {
            tokenRepository.save(type.toToken(IDGenerator.newId(), projectId));
        }
        for (SpacingToken spacing : defaultSpacing()) {
            tokenRepository.save(spacing.toToken(IDGenerator.newId(), projectId));
        }
        for (ShapeToken shape : defaultShapes()) {
            tokenRepository.save(shape.toToken(IDGenerator.newId(), projectId));
        }
        for (ElevationToken elevation : defaultElevations()) {
            tokenRepository.save(elevation.toToken(IDGenerator.newId(), projectId));
        }
    }

    public List<DesignToken> getTokens(String projectId, String kind) {
        seedIfEmpty(projectId);
        return tokenRepository.getByProjectAndKind(projectId, kind);
    }

    public List<ColorToken> getColors(String projectId) {
        List<ColorToken> out = new ArrayList<>();
        for (DesignToken token : getTokens(projectId, DesignToken.KIND_COLOR)) {
            out.add(ColorToken.fromToken(token));
        }
        return out;
    }

    /** Updates a colour token's value (validated hex only). */
    public boolean updateColor(String projectId, String name, String hex) {
        ColorToken color = new ColorToken(name, hex);
        if (!color.isValid()) {
            return false;
        }
        DesignToken existing = tokenRepository.getByName(projectId,
                DesignToken.KIND_COLOR, name);
        String id = existing != null ? existing.getId() : IDGenerator.newId();
        return tokenRepository.save(color.toToken(id, projectId));
    }

    public List<ThemeDefinition> getThemes(String projectId) {
        List<ThemeDefinition> themes = themeRepository.getByProject(projectId);
        if (themes.isEmpty()) {
            ThemeDefinition light = ThemeDefinition.light(
                    IDGenerator.newId(), projectId, "Light");
            ThemeDefinition dark = ThemeDefinition.dark(
                    IDGenerator.newId(), projectId, "Dark");
            themeRepository.save(light);
            themeRepository.save(dark);
            themes.add(light);
            themes.add(dark);
        }
        return themes;
    }

    /**
     * Applies a theme to the project's JSON IR theme metadata and
     * saves it. Explicit per-component colours are untouched.
     */
    public boolean applyTheme(String projectId, ThemeDefinition theme) {
        List<String> problems = ThemeValidator.validate(theme);
        if (!problems.isEmpty()) {
            return false;
        }
        DocumentRepository repo = new DocumentRepository(dbHelper);
        IRDocument document = repo.loadDocument(projectId);
        if (document == null) {
            return false;
        }
        document.setTheme(theme.toThemeModel());
        return repo.saveDocument(document);
    }
}
