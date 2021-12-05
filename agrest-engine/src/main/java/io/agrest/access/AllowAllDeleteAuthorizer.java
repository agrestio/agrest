package io.agrest.access;

/**
 * @since 4.8
 */
final class AllowAllDeleteAuthorizer<T> implements DeleteAuthorizer<T> {

    static final AllowAllDeleteAuthorizer instance = new AllowAllDeleteAuthorizer();

    private AllowAllDeleteAuthorizer() {
    }

    @Override
    public boolean isAllowed(T object) {
        return true;
    }

    @Override
    public boolean allowsAll() {
        return true;
    }

    @Override
    public DeleteAuthorizer<T> andThen(DeleteAuthorizer<T> another) {
        return another;
    }
}
