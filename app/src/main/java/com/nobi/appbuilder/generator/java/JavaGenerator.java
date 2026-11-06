package com.nobi.appbuilder.generator.java;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Java generation for the whole project: one Activity per screen.
 * Resolved ids and known layouts are collected from the generated
 * XML first, so Java validation can check every R reference.
 */
public class JavaGenerator {

    private final GenerationContext context;
    private final ActivityGenerator activities;

    public JavaGenerator(GenerationContext context) {
        this.context = context;
        this.activities = new ActivityGenerator(context);
    }

    /**
     * Generates every Activity. {@code resolvedIds} maps component id
     * -&gt; generated android:id (from the XML stage) and
     * {@code knownLayouts} holds every generated layout name.
     * Returns screen id -&gt; source.
     */
    public Map<String, String> generateAll(Map<String, String> resolvedIds,
                                           Set<String> knownLayouts) {
        Map<String, String> sources = new HashMap<>();
        Set<String> knownIds = new HashSet<>(resolvedIds.values());
        for (ScreenModel screen : context.getDocument().getScreens()) {
            if (screen.getRootComponent() == null) {
                context.addError("java", activities.activityPathFor(screen),
                        screen.getId(), "screen has no root component");
                continue;
            }
            String source = activities.generateActivity(screen, resolvedIds,
                    knownIds, knownLayouts);
            sources.put(screen.getId(), source);
        }
        return sources;
    }
}
