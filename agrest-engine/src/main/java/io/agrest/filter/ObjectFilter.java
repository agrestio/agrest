package io.agrest.filter;

/**
 * Per-entity filter of objects used to implement data access rules.
 *
 * @since 4.8
 */
@FunctionalInterface
public interface ObjectFilter<T> {

    static <T> ObjectFilter<T> trueFilter() {
        return AllowAllObjectFilter.instance;
    }

    boolean isAccessible(T object);

    /**
     * @return whether the filter is a noop and can be ignored
     */
    default boolean allowAll() {
        return false;
    }

    default ObjectFilter<T> and(ObjectFilter<T> another) {
        if (another.allowAll()) {
            return this;
        }

        if (this.allowAll()) {
            return another;
        }

        return o -> this.isAccessible(o) && another.isAccessible(o);
    }
}
