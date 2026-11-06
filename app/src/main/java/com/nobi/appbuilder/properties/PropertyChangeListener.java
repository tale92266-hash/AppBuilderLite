package com.nobi.appbuilder.properties;

import com.nobi.appbuilder.model.ComponentModel;

/** Observer for property edits: the panel reports, the activity reacts. */
public interface PropertyChangeListener {

    /** A property was validated and written to the model. */
    void onPropertyChanged(ComponentModel model, PropertyDefinition definition,
                           PropertyValue oldValue, PropertyValue newValue);

    /** A value failed validation and was not written. */
    void onPropertyValidationFailed(PropertyDefinition definition, String error);
}
