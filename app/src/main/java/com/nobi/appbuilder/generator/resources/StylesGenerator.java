package com.nobi.appbuilder.generator.resources;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.templates.ResourceTemplate;
import com.nobi.appbuilder.model.ThemeModel;

/**
 * res/values/themes.xml from the project ThemeModel. Falls back to
 * AppBuilder Lite's own default palette when the theme has no
 * colours set — the generated theme never depends on AppBuilder's
 * resources.
 */
public class StylesGenerator {

    private final GenerationContext context;

    public StylesGenerator(GenerationContext context) {
        this.context = context;
    }

    public String generate() {
        ThemeModel theme = context.getDocument().getTheme();
        String primary = "#0B4499";
        String primaryDark = "#082F6B";
        String accent = "#0B4499";
        if (theme != null) {
            if (theme.getPrimaryColor() != null
                    && !theme.getPrimaryColor().isEmpty()) {
                primary = theme.getPrimaryColor();
            }
            if (theme.getPrimaryDarkColor() != null
                    && !theme.getPrimaryDarkColor().isEmpty()) {
                primaryDark = theme.getPrimaryDarkColor();
            }
            if (theme.getAccentColor() != null
                    && !theme.getAccentColor().isEmpty()) {
                accent = theme.getAccentColor();
            }
        }
        return ResourceTemplate.themesXml(primary, primaryDark, accent);
    }
}
