package io.agrest.access;

import io.agrest.EntityUpdate;

/**
 * @since 4.8
 */
final class AllowAllCreateAuthorizer<T> implements CreateAuthorizer<T> {

    static final AllowAllCreateAuthorizer instance = new AllowAllCreateAuthorizer();

    private AllowAllCreateAuthorizer() {
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
    public CreateAuthorizer<T> andThen(CreateAuthorizer<T> another) {
        return another;
    }
}
