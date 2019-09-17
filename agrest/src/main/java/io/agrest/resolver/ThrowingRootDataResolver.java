package io.agrest.resolver;

import io.agrest.runtime.processor.select.SelectContext;

/**
 * @param <T>
 * @since 3.4
 */
public class ThrowingRootDataResolver<T> implements RootDataResolver<T> {

    private static final RootDataResolver instance = new ThrowingRootDataResolver();

    public static <T> RootDataResolver<T> getInstance() {
        return instance;
    }

    @Override
    public void assembleQuery(SelectContext<T> context) {
        throw new UnsupportedOperationException(
                "This is a placeholder for the root resolver. " +
                        "A real resolver is needed to read entity '" + context.getEntity().getName() + "'");
    }

    @Override
    public void fetchData(SelectContext<T> context) {
        throw new UnsupportedOperationException(
                "This is a placeholder the root resolver. " +
                        "A real resolver is needed to read entity '" + context.getEntity().getName() + "'");
    }
}
