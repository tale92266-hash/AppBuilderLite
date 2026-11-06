package com.nobi.appbuilder.generator.templates;

/**
 * Deterministic Java Activity template. Placeholders are replaced
 * once, in order, by ActivityGenerator — no string building inside
 * the generated source itself.
 */
public final class ActivityTemplate {

    private ActivityTemplate() {
    }

    public static final String TEMPLATE =
            "package {PACKAGE};\n"
            + "\n"
            + "{IMPORTS}\n"
            + "\n"
            + "public class {CLASS_NAME} extends AppCompatActivity {\n"
            + "\n"
            + "{FIELDS}\n"
            + "    @Override\n"
            + "    protected void onCreate(Bundle savedInstanceState) {\n"
            + "        super.onCreate(savedInstanceState);\n"
            + "        setContentView(R.layout.{LAYOUT_NAME});\n"
            + "\n"
            + "{BINDINGS}\n"
            + "{LISTENERS}\n"
            + "    }\n"
            + "}\n";

    public static String render(String packageName, String className,
                                String imports, String fields,
                                String layoutName, String bindings,
                                String listeners) {
        return TEMPLATE
                .replace("{PACKAGE}", packageName)
                .replace("{CLASS_NAME}", className)
                .replace("{IMPORTS}", imports)
                .replace("{FIELDS}", fields)
                .replace("{LAYOUT_NAME}", layoutName)
                .replace("{BINDINGS}", bindings)
                .replace("{LISTENERS}", listeners);
    }
}
