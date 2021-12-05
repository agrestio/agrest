package io.agrest.access;

/**
 * Per-entity predicate-like object used to implement object DELETE access policies.
 *
 * @since 4.8
 */
public interface DeleteAuthorizer<T> {

    static <T> DeleteAuthorizer<T> allowsAllFilter() {
        return AllowAllDeleteAuthorizer.instance;
    }

    boolean isAllowed(T object);

    /**
     * @return whether the filter is a noop and can be ignored
     */
    default boolean allowsAll() {
        return false;
    }

    default DeleteAuthorizer<T> andThen(DeleteAuthorizer<T> another) {
        if (another.allowsAll()) {
            return this;
        }

        if (this.allowsAll()) {
            return another;
        }

        return o -> this.isAllowed(o) && another.isAllowed(o);
    }
}
