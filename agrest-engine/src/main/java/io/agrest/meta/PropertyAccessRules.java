package io.agrest.meta;

import java.util.Objects;

/**
 * A set of custom property access rules
 *
 * @see AgEntityOverlay#readAccess(PropertyAccessRules)
 * @see AgEntityOverlay#writeAccess(PropertyAccessRules)
 * @since 4.7
 */
@FunctionalInterface
public interface PropertyAccessRules {

    /**
     * Applies a set of access rules encapsula
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
