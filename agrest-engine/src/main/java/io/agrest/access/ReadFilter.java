package io.agrest.access;

/**
 * Per-entity filter used to implement object READ access policies.
 *
 * @since 4.8
 */
@FunctionalInterface
public interface ReadFilter<T> {

    static <T> ReadFilter<T> allowsAllFilter() {
        return AllowAllReadFilter.instance;
    }

    boolean isAllowed(T object);

    /**
     * @return whether the filter is a noop and can be ignored
     */
    default boolean allowsAll() {
        return false;
    }

    default ReadFilter<T> andThen(ReadFilter<T> another) {
        if (another == null || another.allowsAll()) {
            return this;
        }

        if (this.allowsAll()) {
            return another;
        }

        return o -> this.isAllowed(o) && another.isAllowed(o);
    }
}
