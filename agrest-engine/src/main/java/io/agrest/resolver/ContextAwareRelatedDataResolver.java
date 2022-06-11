package io.agrest.resolver;

import io.agrest.RelatedResourceEntity;
import io.agrest.processor.ProcessingContext;
import io.agrest.reader.DataReader;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.function.Function;

/**
 * @since 5.0
 */
public class ContextAwareRelatedDataResolver<T> implements RelatedDataResolver<T> {

    private Function<ProcessingContext<?>, RelatedDataResolver<T>> resolverFactory;
    private volatile RelatedDataResolver<T> delegate;

    public ContextAwareRelatedDataResolver(Function<ProcessingContext<?>, RelatedDataResolver<T>> resolverFactory) {
        this.resolverFactory = resolverFactory;
    }

    @Override
    public void onParentQueryAssembled(RelatedResourceEntity<T> entity, SelectContext<?> context) {
        delegate(context).onParentQueryAssembled(entity, context);
    }

    @Override
    public void onParentDataResolved(RelatedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        delegate(context).onParentDataResolved(entity, parentData, context);
    }

    @Override
    public DataReader dataReader(RelatedResourceEntity<T> entity, ProcessingContext<?> context) {
        return delegate(context).dataReader(entity, context);
    }

    private RelatedDataResolver<T> delegate(ProcessingContext<?> context) {
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
