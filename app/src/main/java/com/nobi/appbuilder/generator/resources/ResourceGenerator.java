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
        result.addTextFile("app/src/main/res/values/colors.xml",
                new ColorsGenerator(context).generate());
        result.addTextFile("app/src/main/res/values/strings.xml",
                new StringsGenerator(context).generate());
        result.addTextFile("app/src/main/res/values/dimens.xml",
                new DimensionsGenerator(context).generate());
        result.addTextFile("app/src/main/res/values/themes.xml",
                new StylesGenerator(context).generate());
        for (Map.Entry<String, String> entry
                : new DrawableGenerator(context).generate().entrySet()) {
            result.addTextFile("app/src/main/res/drawable/" + entry.getKey(),
                    entry.getValue());
        }
        for (AssetGenerator.GeneratedAsset asset
                : new AssetGenerator(context).generate()) {
            result.addBinaryFile(asset.path, asset.bytes);
        }
    }
}
