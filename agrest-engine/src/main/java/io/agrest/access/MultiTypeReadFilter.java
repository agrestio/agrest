package io.agrest.access;

import java.util.function.Function;

/**
 * @param <T>
 * @since 5.0
 */
public class MultiTypeReadFilter<T> implements ReadFilter<T> {

    private final ReadFilter<T> nullFilter;
    private final Function<Class<?>, ReadFilter<T>> filterFactory;

    public MultiTypeReadFilter(Function<Class<?>, ReadFilter<T>> filterFactory, ReadFilter<T> nullFilter) {
        this.nullFilter = nullFilter;
        this.filterFactory = filterFactory;
    }

    @Override
    public boolean isAllowed(T object) {

        if (object == null) {
            return nullFilter.isAllowed(null);
        }

        ReadFilter<T> filter = filterFactory.apply(object.getClass());
        return filter != null
                ? filter.isAllowed(object)
                // unknown type - just deny it... TODO: print a debug message?
                : false;
    }
}
