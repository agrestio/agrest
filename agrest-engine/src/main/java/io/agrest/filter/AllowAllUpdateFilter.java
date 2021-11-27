package io.agrest.filter;

import io.agrest.EntityUpdate;

/**
 * @since 4.8
 */
final class AllowAllUpdateFilter<T> implements UpdateFilter<T> {

    static final AllowAllUpdateFilter instance = new AllowAllUpdateFilter();

    private AllowAllUpdateFilter() {
    }

    @Override
    public boolean isAllowed(T object, EntityUpdate<T> update) {
        return true;
    }

    @Override
    public boolean allowsAll() {
        return true;
    }

    @Override
    public UpdateFilter<T> andThen(UpdateFilter<T> another) {
        return another;
    }
}
