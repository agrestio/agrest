package io.agrest.filter;

/**
 * @since 4.8
 */
final class AllowAllObjectFilter implements ObjectFilter {

    static final AllowAllObjectFilter instance = new AllowAllObjectFilter();

    private AllowAllObjectFilter() {
    }

    @Override
    public boolean isAccessible(Object object) {
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
