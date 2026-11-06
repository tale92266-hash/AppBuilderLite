package com.nobi.appbuilder.export.validation;

import com.nobi.appbuilder.export.PackagingError;
import com.nobi.appbuilder.generator.GenerationResult;
import com.nobi.appbuilder.generator.xml.XMLValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Manifest checks: application element, launcher, exported flags. */
public class ManifestValidator {

    public List<PackagingError> validate(GenerationResult result) {
        List<PackagingError> problems = new ArrayList<>();
        String manifest = result.getTextFile("app/src/main/AndroidManifest.xml");
        if (manifest == null) {
            problems.add(PackagingError.error("manifest",
                    "app/src/main/AndroidManifest.xml",
                    "Manifest was not generated"));
            return problems;
        }
        String parseError = XMLValidator.parse(manifest);
        if (parseError != null) {
            problems.add(PackagingError.error("manifest",
                    "app/src/main/AndroidManifest.xml",
                    "Manifest does not parse: " + parseError));
            return problems;
        }
        if (!manifest.contains("<application")) {
            problems.add(PackagingError.error("manifest",
                    "app/src/main/AndroidManifest.xml",
                    "No <application> element"));
        }
        if (!manifest.contains("android.intent.action.MAIN")
                || !manifest.contains("android.intent.category.LAUNCHER")) {
            problems.add(PackagingError.error("manifest",
                    "app/src/main/AndroidManifest.xml",
                    "No launcher Activity (MAIN/LAUNCHER intent-filter)"));
        }
        // The launcher intent-filter is a child element, not part of
        // the <activity> opening tag, so check exported on the tag
        // itself: a manifest with a launcher filter must have an
        // exported activity.
        Matcher activities = Pattern.compile(
                "<activity[^>]*android:name=\"([^\"]+)\"[^>]*>")
                .matcher(manifest);
        boolean anyActivity = false;
        boolean anyExported = false;
        while (activities.find()) {
            anyActivity = true;
            if (activities.group(0).contains("android:exported=\"true\"")) {
                anyExported = true;
            }
        }
        if (!anyActivity) {
            problems.add(PackagingError.error("manifest",
                    "app/src/main/AndroidManifest.xml",
                    "No <activity> declarations"));
        } else if (!anyExported) {
            problems.add(PackagingError.error("manifest",
                    "app/src/main/AndroidManifest.xml",
                    "Launcher activity must be exported=\"true\""));
        }
        return problems;
    }
}
