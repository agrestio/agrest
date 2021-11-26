package io.agrest.access;

import java.util.Objects;

/**
 * Encapsulates logic for creating custom property access rules for a single entity. The logic is implemented in the
 * {@link #apply(PropertyAccessBuilder)} method, where custom code can invoke various access rules on an instance of
 * {@link PropertyAccessBuilder}.
 *
 * @see io.agrest.SelectBuilder#propertyAccess(Class, PropertyAccessRules)
 * @since 4.7
 */
@FunctionalInterface
public interface PropertyAccessRules {

    /**
     * Configures access rules of the provided {@link PropertyAccessBuilder}.
     *
     * @param accessConfig
     */
    void apply(PropertyAccessBuilder accessConfig);

    default PropertyAccessRules andThen(PropertyAccessRules after) {
        Objects.requireNonNull(after);
        return pa -> {
            apply(pa);
            after.apply(pa);
        };
    }
}
