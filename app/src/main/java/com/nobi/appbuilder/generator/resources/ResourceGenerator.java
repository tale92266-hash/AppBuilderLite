package com.nobi.appbuilder.generator.resources;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.GenerationResult;

import java.util.Map;

/**
 * Orchestrates resource generation. Must run AFTER the layout stage,
 * because the shared collector is filled while layouts are generated.
 */
public class ResourceGenerator {

    private final GenerationContext context;

    public ResourceGenerator(GenerationContext context) {
        this.context = context;
    }

    public void generateInto(GenerationResult result) {
        // Drawables FIRST: DrawableGenerator registers its template
        // colours (color_primary, color_surface) in the shared
        // collector, and colors.xml is written from that collector.
        // Writing colors.xml before the drawables left those @color
        // references unresolved and failed every export at the
        // resource-validation stage (0 files). The layout stage is
        // the only other collector writer, and it runs before this.
        Map<String, String> drawables =
                new DrawableGenerator(context).generate();
        result.addTextFile("app/src/main/res/values/colors.xml",
                new ColorsGenerator(context).generate());
        result.addTextFile("app/src/main/res/values/strings.xml",
                new StringsGenerator(context).generate());
        result.addTextFile("app/src/main/res/values/dimens.xml",
                new DimensionsGenerator(context).generate());
        result.addTextFile("app/src/main/res/values/themes.xml",
                new StylesGenerator(context).generate());
        for (Map.Entry<String, String> entry : drawables.entrySet()) {
            result.addTextFile("app/src/main/res/drawable/" + entry.getKey(),
                    entry.getValue());
        }
        for (AssetGenerator.GeneratedAsset asset
                : new AssetGenerator(context).generate()) {
            result.addBinaryFile(asset.path, asset.bytes);
        }
    }
}
