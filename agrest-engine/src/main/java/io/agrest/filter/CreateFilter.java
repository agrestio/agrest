package io.agrest.filter;

import io.agrest.EntityUpdate;

/**
 * Per-entity filter used to implement object CREATE access policies.
 *
 * @since 4.8
 */
public interface CreateFilter<T> {

    static <T> CreateFilter<T> allowsAllFilter() {
        return AllowAllCreateFilter.instance;
    }

    boolean isAllowed(EntityUpdate<T> update);

    /**
     * @return whether the filter is a noop and can be ignored
     */
    default boolean allowsAll() {
        return false;
    }

    default CreateFilter<T> andThen(CreateFilter<T> another) {
        if (another.allowsAll()) {
            return this;
        }

        if (this.allowsAll()) {
            return another;
        }

        return u -> this.isAllowed(u) && another.isAllowed(u);
    }
}
