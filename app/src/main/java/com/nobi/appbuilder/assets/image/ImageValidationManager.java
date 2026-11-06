package com.nobi.appbuilder.assets.image;

import com.nobi.appbuilder.assets.AssetValidationManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Image-specific validation: magic bytes, size, decodability. */
public final class ImageValidationManager {

    private ImageValidationManager() {
    }

    public static List<String> validate(File file) {
        List<String> errors = AssetValidationManager.validateFile(
                file, com.nobi.appbuilder.assets.AssetType.IMAGE);
        if (!errors.isEmpty()) {
            return errors;
        }
        if (ImageMetadataReader.read(file, "") == null) {
            errors.add("Image could not be decoded");
        }
        return errors;
    }

    public static boolean isSupportedImage(File file) {
        return new ArrayList<>(validate(file)).isEmpty();
    }
}
