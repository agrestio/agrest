package io.agrest.access;

import io.agrest.EntityUpdate;

/**
 * @since 4.8
 */
final class AllowAllUpdateAuthorizer<T> implements UpdateAuthorizer<T> {

    static final AllowAllUpdateAuthorizer instance = new AllowAllUpdateAuthorizer();

    private AllowAllUpdateAuthorizer() {
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
    public UpdateAuthorizer<T> andThen(UpdateAuthorizer<T> another) {
        return another;
    }
}
