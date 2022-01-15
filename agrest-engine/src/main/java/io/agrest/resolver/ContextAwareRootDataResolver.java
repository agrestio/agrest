package io.agrest.resolver;

import io.agrest.runtime.processor.select.SelectContext;

import java.util.function.Function;

/**
 * @since 5.0
 */
public class ContextAwareRootDataResolver<T> implements RootDataResolver<T> {

    private Function<SelectContext<T>, RootDataResolver<T>> resolverFactory;
    private volatile RootDataResolver<T> delegate;

    public ContextAwareRootDataResolver(Function<SelectContext<T>, RootDataResolver<T>> resolverFactory) {
        this.resolverFactory = resolverFactory;
    }

    @Override
    public void assembleQuery(SelectContext<T> context) {
        delegate(context).assembleQuery(context);
    }

    @Override
    public void fetchData(SelectContext<T> context) {
        delegate(context).fetchData(context);
    }

    private RootDataResolver<T> delegate(SelectContext<T> context) {
        if (delegate == null) {
            synchronized (delegate) {
                if (delegate == null) {
                    delegate = resolverFactory.apply(context);
                    resolverFactory = null;
                }
            }
        }

        return delegate;
    }
}
