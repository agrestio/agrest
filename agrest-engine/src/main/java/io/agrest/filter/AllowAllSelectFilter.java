package io.agrest.filter;

/**
 * @since 4.8
 */
final class AllowAllSelectFilter<T> implements SelectFilter<T> {

    static final AllowAllSelectFilter instance = new AllowAllSelectFilter<>();

    private AllowAllSelectFilter() {
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
    public SelectFilter<T> andThen(SelectFilter<T> another) {
        return another;
    }
}
