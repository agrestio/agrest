package io.agrest.meta;

import java.util.Objects;

/**
 * A custom property access rule
 *
 * @see AgEntityOverlay#readAccess(PropertyAccessRule)
 * @see AgEntityOverlay#writeAccess(PropertyAccessRule)
 * @since 4.7
 */
@FunctionalInterface
public interface PropertyAccessRule {

    void apply(PropertyAccess accessConfig);

    default PropertyAccessRule andThen(PropertyAccessRule after) {
        Objects.requireNonNull(after);
        return pa -> {
            apply(pa);
            after.apply(pa);
        };
    }
}
