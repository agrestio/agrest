package io.agrest.access;

import io.agrest.EntityUpdate;

/**
 * Per-entity predicate-like object used to implement object CREATE access policies.
 *
 * @since 4.8
 */
public interface CreateAuthorizer<T> {

    static <T> CreateAuthorizer<T> allowsAllFilter() {
        return AllowAllCreateAuthorizer.instance;
    }

    boolean isAllowed(EntityUpdate<T> update);

    /**
     * @return whether the filter is a noop and can be ignored
     */
    default boolean allowsAll() {
        return false;
    }

    default CreateAuthorizer<T> andThen(CreateAuthorizer<T> another) {
        if (another.allowsAll()) {
            return this;
        }

        if (this.allowsAll()) {
            return another;
        }

        return u -> this.isAllowed(u) && another.isAllowed(u);
    }
}
