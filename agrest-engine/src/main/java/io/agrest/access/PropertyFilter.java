package io.agrest.access;

import java.util.Objects;

/**
 * Encapsulates logic for creation of custom property access rules for a single entity. The logic is implemented in the
 * {@link #apply(PropertyFilteringRulesBuilder)} method, where custom code invokes methods on an instance of
 * {@link PropertyFilteringRulesBuilder} to define access rules.
 *
 * @see io.agrest.SelectBuilder#propFilter(Class, PropertyFilter)
 * @since 4.7
 */
@FunctionalInterface
public interface PropertyFilter {

    /**
     * Configures property access rules via the provided {@link PropertyFilteringRulesBuilder}.
     */
    void apply(PropertyFilteringRulesBuilder accessConfig);

    default PropertyFilter andThen(PropertyFilter after) {
        Objects.requireNonNull(after);
        return pa -> {
            apply(pa);
            after.apply(pa);
        };
    }
}
