package io.agrest.resolver;

import io.agrest.NestedResourceEntity;
import io.agrest.processor.ProcessingContext;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.function.Function;

/**
 * @since 5.0
 */
public class ContextAwareNestedDataResolver<T> implements NestedDataResolver<T> {

    private Function<ProcessingContext<?>, NestedDataResolver<T>> resolverFactory;
    private volatile NestedDataResolver<T> delegate;

    public ContextAwareNestedDataResolver(Function<ProcessingContext<?>, NestedDataResolver<T>> resolverFactory) {
        this.resolverFactory = resolverFactory;
    }

    @Override
    public void onParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
        delegate(context).onParentQueryAssembled(entity, context);
    }

    @Override
    public void onParentDataResolved(NestedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        delegate(context).onParentDataResolved(entity, parentData, context);
    }

    @Override
    public PropertyReader reader(NestedResourceEntity<T> entity, ProcessingContext<?> context) {
        return delegate(context).reader(entity, context);
    }

    private NestedDataResolver<T> delegate(ProcessingContext<?> context) {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    delegate = resolverFactory.apply(context);
                    resolverFactory = null;
                }
            }
        }

        return delegate;
    }
}
