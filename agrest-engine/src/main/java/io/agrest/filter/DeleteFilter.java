package io.agrest.filter;

/**
 * Per-entity filter used to implement object DELETE access policies.
 *
 * @since 4.8
 */
public interface DeleteFilter<T> {

    static <T> DeleteFilter<T> allowsAllFilter() {
        return AllowAllDeleteFilter.instance;
    }

    boolean isAllowed(T object);

    /**
     * @return whether the filter is a noop and can be ignored
     */
    default boolean allowsAll() {
        return false;
    }

    default DeleteFilter<T> andThen(DeleteFilter<T> another) {
        if (another.allowsAll()) {
            return this;
        }

        if (this.allowsAll()) {
            return another;
        }

        return o -> this.isAllowed(o) && another.isAllowed(o);
    }
}
