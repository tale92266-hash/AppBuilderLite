package com.nobi.appbuilder.generator.java;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.mapping.EventMappingRegistry;
import com.nobi.appbuilder.generator.xml.XMLIdResolver;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.EventModel;

/**
 * Turns EventModels into Java listener code. Only the predefined
 * actions in EventMappingRegistry are generated; the code is built
 * from fixed templates with escaped literals, never by compiling or
 * executing anything from the document.
 */
public class EventCodeGenerator {

    private final GenerationContext context;
    private final NavigationCodeGenerator navigation;

    public EventCodeGenerator(GenerationContext context) {
        this.context = context;
        this.navigation = new NavigationCodeGenerator(context);
    }

    /**
     * Listener code for one component's events, indented for the
     * onCreate body. Returns "" when the component has no generatable
     * events. Invalid events are reported, not generated.
     */
    public String listenersFor(ComponentModel model, String fieldName,
                               String viewId) {
        StringBuilder out = new StringBuilder();
        for (EventModel event : model.getEvents()) {
            boolean targetExists = event.getTargetScreenId() == null
                    || navigation.isValidDestination(event.getTargetScreenId());
            String problem = EventMappingRegistry.validateEvent(event, targetExists);
            if (problem != null) {
                context.addWarning("java", null, model.getId(),
                        "event skipped: " + problem);
                continue;
            }
            String body = actionsFor(event, model);
            if (body == null || body.isEmpty()) {
                continue;
            }
            String trigger = event.getEventType();
            if (EventModel.EVENT_CLICK.equals(trigger)) {
                out.append("        ").append(fieldName)
                        .append(".setOnClickListener(v -> {\n");
                out.append(body);
                out.append("        });\n");
            } else if (EventModel.EVENT_LONG_CLICK.equals(trigger)) {
                out.append("        ").append(fieldName)
                        .append(".setOnLongClickListener(v -> {\n");
                out.append(body);
                out.append("            return true;\n");
                out.append("        });\n");
            } else if (EventModel.EVENT_TEXT_CHANGED.equals(trigger)) {
                out.append("        ").append(fieldName)
                        .append(".addTextChangedListener(new TextWatcher() {\n");
                out.append("            @Override\n");
                out.append("            public void beforeTextChanged(CharSequence s, "
                        + "int start, int count, int after) {\n");
                out.append("            }\n");
                out.append("\n");
                out.append("            @Override\n");
                out.append("            public void onTextChanged(CharSequence s, "
                        + "int start, int before, int count) {\n");
                out.append("            }\n");
                out.append("\n");
                out.append("            @Override\n");
                out.append("            public void afterTextChanged(Editable s) {\n");
                out.append(body);
                out.append("            }\n");
                out.append("        });\n");
            }
        }
        return out.toString();
    }

    /** The statements for one event action, indented 12 spaces. */
    private String actionsFor(EventModel event, ComponentModel source) {
        String action = event.getAction();
        String field = XMLIdResolver.fieldNameFor(source.getId());
        String message = event.getMessage();
        switch (action) {
            case EventMappingRegistry.ACTION_TOAST:
            case EventMappingRegistry.ACTION_SHOW_TOAST:
                return "            Toast.makeText(this, "
                        + JavaSourceValidator.javaString(
                                message == null ? "" : message)
                        + ", Toast.LENGTH_SHORT).show();\n";
            case EventMappingRegistry.ACTION_NAVIGATE: {
                String intent = navigation.intentFor(event.getTargetScreenId());
                if (intent == null) {
                    return null;
                }
                return "            startActivity(" + intent + ");\n";
            }
            case EventMappingRegistry.ACTION_CHANGE_TEXT:
                return "            " + field + ".setText("
                        + JavaSourceValidator.javaString(
                                message == null ? "" : message) + ");\n";
            case EventMappingRegistry.ACTION_VALIDATE_INPUT: {
                String text = message == null || message.isEmpty()
                        ? "Please enter a value" : message;
                return "            if (" + field + ".getText().toString().trim()"
                        + ".isEmpty()) {\n"
                        + "                Toast.makeText(this, "
                        + JavaSourceValidator.javaString(text)
                        + ", Toast.LENGTH_SHORT).show();\n"
                        + "                return;\n"
                        + "            }\n";
            }
            case EventMappingRegistry.ACTION_FINISH_ACTIVITY:
            case EventMappingRegistry.ACTION_FINISH:
                return "            finish();\n";
            case EventMappingRegistry.ACTION_SET_VISIBILITY:
            case EventMappingRegistry.ACTION_CHANGE_VISIBILITY: {
                String visibility = "View.GONE";
                if ("visible".equalsIgnoreCase(message)) {
                    visibility = "View.VISIBLE";
                } else if ("invisible".equalsIgnoreCase(message)) {
                    visibility = "View.INVISIBLE";
                }
                return "            " + field + ".setVisibility(" + visibility
                        + ");\n";
            }
            case EventMappingRegistry.ACTION_OPEN_URL:
                return "            startActivity(new Intent(Intent.ACTION_VIEW, "
                        + "Uri.parse("
                        + JavaSourceValidator.javaString(message) + ")));\n";
            case EventMappingRegistry.ACTION_SET_ENABLED: {
                boolean enabled = !"false".equalsIgnoreCase(message);
                return "            " + field + ".setEnabled(" + enabled + ");\n";
            }
            case EventMappingRegistry.ACTION_NONE:
            default:
                return "";
        }
    }
}
