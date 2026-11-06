package com.nobi.appbuilder.export.zip;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Verifies a completed ZIP: it opens, has no duplicate or unsafe
 * entry names, and contains every file an Android Studio project
 * needs. Verification failure means the export failed, even when a
 * ZIP file was written.
 */
public class ZipValidationManager {

    /** Files every exported project must contain. */
    public static final String[] REQUIRED_ENTRIES = {
            "settings.gradle",
            "build.gradle",
            "gradle.properties",
            "app/build.gradle",
            "app/src/main/AndroidManifest.xml",
            "app/src/main/res/values/colors.xml",
            "app/src/main/res/values/strings.xml",
            "app/src/main/res/values/dimens.xml",
            "app/src/main/res/values/themes.xml",
            "README.md"
    };

    /** Returns problems; empty means the ZIP is valid. */
    public List<String> validate(File zipFile) {
        List<String> problems = new ArrayList<>();
        if (zipFile == null || !zipFile.exists()) {
            problems.add("ZIP file does not exist");
            return problems;
        }
        if (zipFile.length() == 0) {
            problems.add("ZIP file is empty");
            return problems;
        }
        Set<String> seen = new HashSet<>();
        try (ZipFile zip = new ZipFile(zipFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            int count = 0;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                count++;
                String name = entry.getName();
                if (name.startsWith("/") || name.contains("..")) {
                    problems.add("Unsafe entry name: " + name);
                }
                if (!seen.add(name)) {
                    problems.add("Duplicate ZIP entry: " + name);
                }
                if (entry.isDirectory()) {
                    continue;
                }
                // Entry content must be readable.
                try {
                    zip.getInputStream(entry).close();
                } catch (IOException e) {
                    problems.add("Unreadable entry " + name + ": "
                            + e.getMessage());
                }
            }
            if (count == 0) {
                problems.add("ZIP contains no entries");
            }
            for (String required : REQUIRED_ENTRIES) {
                if (!seen.contains(required)) {
                    problems.add("Missing required file: " + required);
                }
            }
        } catch (IOException e) {
            problems.add("ZIP cannot be opened: " + e.getMessage());
        }
        return problems;
    }
}
