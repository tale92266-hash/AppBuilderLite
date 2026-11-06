package com.nobi.appbuilder.assets.fonts;

import com.nobi.appbuilder.assets.AssetValidationManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Font validation: TTF/OTF magic bytes only. Android cannot
 * consume other formats, so they are rejected — never claimed
 * as supported.
 */
public final class FontValidator {

    private FontValidator() {
    }

    public static List<String> validate(File file) {
        List<String> errors = AssetValidationManager.validateFile(
                file, com.nobi.appbuilder.assets.AssetType.FONT);
        if (!errors.isEmpty()) {
            return errors;
        }
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".ttf") && !name.endsWith(".otf")) {
            errors.add("Only TTF and OTF fonts are supported");
        }
        return errors;
    }

    public static boolean isSupportedFont(File file) {
        return new ArrayList<>(validate(file)).isEmpty();
    }
}
