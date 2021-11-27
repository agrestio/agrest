package io.agrest.filter;

/**
 * A filter of objects that defines an access policy for a given entity.
 *
 * @since 4.8
 */
@FunctionalInterface
public interface ObjectFilter<T> {

    static <T> ObjectFilter<T> trueFilter() {
        return AllowAllObjectFilter.instance;
    }

    boolean isAllowed(T object);

    /**
     * @return whether the filter is a noop and can be ignored
     */
    default boolean allowsAll() {
        return false;
    }

    default ObjectFilter<T> andThen(ObjectFilter<T> another) {
        if (another.allowsAll()) {
            return this;
        }

        if (this.allowsAll()) {
            return another;
        }

        return o -> this.isAllowed(o) && another.isAllowed(o);
    }
}
