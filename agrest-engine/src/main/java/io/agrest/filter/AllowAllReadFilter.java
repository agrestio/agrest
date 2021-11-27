package io.agrest.filter;

/**
 * @since 4.8
 */
final class AllowAllReadFilter<T> implements ReadFilter<T> {

    static final AllowAllReadFilter instance = new AllowAllReadFilter<>();

    private AllowAllReadFilter() {
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
    public ReadFilter<T> andThen(ReadFilter<T> another) {
        return another;
    }
}
