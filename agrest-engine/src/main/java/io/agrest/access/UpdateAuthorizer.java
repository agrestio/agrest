package io.agrest.access;

import io.agrest.EntityUpdate;

/**
 * Per-entity predicate-like object used to implement object UPDATE access policies.
 *
 * @since 4.8
 */
public interface UpdateAuthorizer<T> {

    static <T> UpdateAuthorizer<T> allowsAllFilter() {
        return AllowAllUpdateAuthorizer.instance;
    }

    boolean isAllowed(T object, EntityUpdate<T> update);

    /**
     * @return whether the filter is a noop and can be ignored
     */
    default boolean allowsAll() {
        return false;
    }

    default UpdateAuthorizer<T> andThen(UpdateAuthorizer<T> another) {
        if (another.allowsAll()) {
            return this;
        }

        if (this.allowsAll()) {
            return another;
        }

        return (o, u) -> this.isAllowed(o, u) && another.isAllowed(o, u);
    }
}
