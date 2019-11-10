package io.agrest.resolver;

import io.agrest.NestedResourceEntity;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 3.4
 */
public abstract class BaseNestedDataResolver<T> extends BaseDataResolver implements NestedDataResolver<T> {

    @Override
    public void onParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
        doOnParentQueryAssembled(entity, context);
        afterQueryAssembled(entity, context);
    }

    @Override
    public void onParentDataResolved(NestedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        Iterable<T> result = doOnParentDataResolved(entity, parentData, context);

        // unlike BaseRootDataResolver, we are not saving the result in the entity. Instead it is passed between parent
        // and child resolvers

        afterDataFetched(entity, result, context);
    }

    @Override
    public PropertyReader reader(NestedResourceEntity<T> entity) {
        return null;
    }

    protected abstract void doOnParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context);

    protected abstract Iterable<T> doOnParentDataResolved(
            NestedResourceEntity<T> entity,
            Iterable<?> parentData,
            SelectContext<?> context);
}
