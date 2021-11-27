package io.agrest.filter;

/**
 * @since 4.8
 */
final class AllowAllObjectFilter<T> implements ObjectFilter<T> {

    static final AllowAllObjectFilter instance = new AllowAllObjectFilter<>();

    private AllowAllObjectFilter() {
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
    public ObjectFilter<T> andThen(ObjectFilter<T> another) {
        return another;
    }
}
