package com.nobi.appbuilder.library.templates;

/** Starter-template categories. */
public enum TemplateCategory {
    AUTHENTICATION("Authentication"),
    DASHBOARD("Dashboard"),
    PROFILE("Profile"),
    SETTINGS("Settings"),
    PRODUCT_LISTING("Product Listing"),
    PRODUCT_DETAILS("Product Details"),
    SHOPPING_CART("Shopping Cart"),
    CHECKOUT("Checkout"),
    CONTACT("Contact"),
    FORMS("Forms"),
    EMPTY("Empty Screen");

    private final String displayName;

    TemplateCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static TemplateCategory fromName(String name) {
        if (name == null) {
            return EMPTY;
        }
        for (TemplateCategory category : values()) {
            if (category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return EMPTY;
    }
}
