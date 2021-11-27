package io.agrest.filter;

import io.agrest.EntityUpdate;

/**
 * @since 4.8
 */
final class AllowAllCreateFilter<T> implements CreateFilter<T> {

    static final AllowAllCreateFilter instance = new AllowAllCreateFilter();

    private AllowAllCreateFilter() {
    }

    @Override
    public boolean isAllowed(EntityUpdate<T> update) {
        return true;
    }

    @Override
    public boolean allowsAll() {
        return true;
    }

    @Override
    public CreateFilter<T> andThen(CreateFilter<T> another) {
        return another;
    }
}
