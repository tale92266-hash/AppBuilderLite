package com.nobi.appbuilder.export.validation;

import com.nobi.appbuilder.export.PackagingError;
import com.nobi.appbuilder.generator.GenerationResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Every @string/@color/@dimen/@drawable reference must resolve. */
public class ResourceValidator {

    public List<PackagingError> validate(GenerationResult result) {
        List<PackagingError> problems = new ArrayList<>();
        Set<String> strings = new HashSet<>();
        Set<String> colors = new HashSet<>();
        Set<String> dimens = new HashSet<>();
        Set<String> drawables = new HashSet<>();
        strings.add("app_name");
        for (String path : result.getAllPaths()) {
            String text = result.getTextFile(path);
            if (text == null) {
                continue;
            }
            if (path.endsWith("strings.xml")) {
                strings.addAll(findAll(text, "<string name=\"([A-Za-z0-9_]+)\""));
            }
            if (path.endsWith("colors.xml")) {
                colors.addAll(findAll(text, "<color name=\"([A-Za-z0-9_]+)\""));
            }
            if (path.endsWith("dimens.xml")) {
                dimens.addAll(findAll(text, "<dimen name=\"([A-Za-z0-9_]+)\""));
            }
            if (path.contains("/drawable/")) {
                drawables.add(fileBaseName(path));
            }
            if (path.endsWith(".png") || path.endsWith(".jpg")
                    || path.endsWith(".jpeg") || path.endsWith(".webp")
                    || path.endsWith(".gif")) {
                drawables.add(fileBaseName(path));
            }
        }
        for (String path : result.getAllPaths()) {
            String text = result.getTextFile(path);
            if (text == null) {
                continue;
            }
            checkRefs(text, "@string/", strings, path, problems);
            checkRefs(text, "@color/", colors, path, problems);
            checkRefs(text, "@dimen/", dimens, path, problems);
            checkRefs(text, "@drawable/", drawables, path, problems);
        }
        return problems;
    }

    private void checkRefs(String text, String prefix, Set<String> known,
                           String path, List<PackagingError> problems) {
        for (String ref : findAll(text,
                Pattern.quote(prefix) + "([A-Za-z0-9_]+)")) {
            if (!known.contains(ref)) {
                problems.add(PackagingError.error("resources", path,
                        "Unresolved reference " + prefix + ref));
            }
        }
    }

    private Set<String> findAll(String text, String regex) {
        Set<String> found = new HashSet<>();
        Matcher matcher = Pattern.compile(regex).matcher(text);
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        return found;
    }

    private String fileBaseName(String path) {
        int slash = path.lastIndexOf('/');
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(0, dot) : name;
    }
}
