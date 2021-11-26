package io.agrest.filter;

/**
 * @since 4.8
 */
final class AllowAllObjectFilter<T> implements ObjectFilter<T> {

    static final AllowAllObjectFilter instance = new AllowAllObjectFilter();

    private AllowAllObjectFilter() {
    }

    @Override
    public boolean isAccessible(T object) {
        return true;
    }

    @Override
    public boolean allowAll() {
        return true;
    }

    @Override
    public ObjectFilter and(ObjectFilter another) {
        return another;
    }
}
