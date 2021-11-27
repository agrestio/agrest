package io.agrest.filter;

import io.agrest.EntityUpdate;

/**
 * Per-entity filter used to implement object UPDATE access policies.
 *
 * @since 4.8
 */
public interface UpdateFilter<T> {

    static <T> UpdateFilter<T> allowsAllFilter() {
        return AllowAllUpdateFilter.instance;
    }

    boolean isAllowed(T object, EntityUpdate<T> update);

    /**
     * @return whether the filter is a noop and can be ignored
     */
    default boolean allowsAll() {
        return false;
    }

    default UpdateFilter<T> andThen(UpdateFilter<T> another) {
        if (another.allowsAll()) {
            return this;
        }

        if (this.allowsAll()) {
            return another;
        }

        return (o, u) -> this.isAllowed(o, u) && another.isAllowed(o, u);
    }
}
