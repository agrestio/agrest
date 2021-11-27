package io.agrest.filter;

/**
 * @since 4.8
 */
final class AllowAllDeleteFilter<T> implements DeleteFilter<T> {

    static final AllowAllDeleteFilter instance = new AllowAllDeleteFilter();

    private AllowAllDeleteFilter() {
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
    public DeleteFilter<T> andThen(DeleteFilter<T> another) {
        return another;
    }
}
