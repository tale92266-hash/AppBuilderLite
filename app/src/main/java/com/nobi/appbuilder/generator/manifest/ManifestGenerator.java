package com.nobi.appbuilder.generator.manifest;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.xml.XMLDocumentWriter;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AndroidManifest.xml for the generated project: one activity per
 * screen, the launcher screen exported with the MAIN/LAUNCHER
 * filter, INTERNET only when an openUrl event exists. Screen
 * orientation is emitted only for landscape screens.
 */
public class ManifestGenerator {

    private final GenerationContext context;

    public ManifestGenerator(GenerationContext context) {
        this.context = context;
    }

    public String generate() {
        XMLDocumentWriter writer = new XMLDocumentWriter();
        Map<String, String> manifestAttrs = new LinkedHashMap<>();
        manifestAttrs.put("xmlns:android",
                "http://schemas.android.com/apk/res/android");
        manifestAttrs.put("package", context.getPackageName());
        writer.startElement("manifest", manifestAttrs);
        if (context.needsInternet()) {
            Map<String, String> perm = new LinkedHashMap<>();
            perm.put("android:name", "android.permission.INTERNET");
            writer.emptyElement("uses-permission", perm);
        }
        Map<String, String> appAttrs = new LinkedHashMap<>();
        appAttrs.put("android:label", "@string/app_name");
        appAttrs.put("android:icon", "@mipmap/ic_launcher");
        appAttrs.put("android:theme", "@style/AppTheme");
        appAttrs.put("android:allowBackup", "true");
        appAttrs.put("android:supportsRtl", "true");
        writer.startElement("application", appAttrs);
        String launcherId = null;
        for (ScreenModel screen : context.getDocument().getScreens()) {
            if (screen.isLauncher()) {
                launcherId = screen.getId();
                break;
            }
        }
        if (launcherId == null && !context.getDocument().getScreens().isEmpty()) {
            launcherId = context.getDocument().getScreens().get(0).getId();
        }
        for (ScreenModel screen : context.getDocument().getScreens()) {
            Map<String, String> attrs = new LinkedHashMap<>();
            attrs.put("android:name",
                    "." + context.activityClassFor(screen.getId()));
            boolean isLauncher = screen.getId().equals(launcherId);
            attrs.put("android:exported", isLauncher ? "true" : "false");
            String orientation = screen.getOrientation();
            if ("landscape".equalsIgnoreCase(orientation)) {
                attrs.put("android:screenOrientation", "landscape");
            }
            if (!isLauncher) {
                writer.emptyElement("activity", attrs);
                continue;
            }
            writer.startElement("activity", attrs);
            writer.startElement("intent-filter");
            Map<String, String> action = new LinkedHashMap<>();
            action.put("android:name", "android.intent.action.MAIN");
            writer.emptyElement("action", action);
            Map<String, String> category = new LinkedHashMap<>();
            category.put("android:name",
                    "android.intent.category.LAUNCHER");
            writer.emptyElement("category", category);
            writer.endElement();
            writer.endElement();
        }
        writer.endElement();
        writer.endElement();
        return writer.build();
    }
}
