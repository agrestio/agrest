package io.agrest.resolver;

import io.agrest.RelatedResourceEntity;
import io.agrest.processor.ProcessingContext;
import io.agrest.reader.DataReader;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 3.4
 */
public abstract class BaseRelatedDataResolver<T> extends BaseDataResolver implements RelatedDataResolver<T> {

    @Override
    public void onParentQueryAssembled(RelatedResourceEntity<T> entity, SelectContext<?> context) {
        doOnParentQueryAssembled(entity, context);
        afterQueryAssembled(entity, context);
    }

    @Override
    public void onParentDataResolved(RelatedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        Iterable<T> result = doOnParentDataResolved(entity, parentData, context);

        // unlike BaseRootDataResolver, we are not saving the result in the entity. Instead it is passed between parent
        // and child resolvers

        afterDataFetched(entity, result, context);
    }

    @Override
    public abstract DataReader dataReader(RelatedResourceEntity<T> entity, ProcessingContext<?> context);

    protected abstract void doOnParentQueryAssembled(RelatedResourceEntity<T> entity, SelectContext<?> context);

    protected abstract Iterable<T> doOnParentDataResolved(
            RelatedResourceEntity<T> entity,
            Iterable<?> parentData,
            SelectContext<?> context);
}
