package io.agrest.resolver;

import io.agrest.NestedResourceEntity;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.List;

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
        List<T> result = doOnParentDataResolved(entity, parentData, context);
        
        // note that unlike BaseRootDataResolver, we are not saving the result in the entity. Implementor will need to
        // figure out what to do with the result

        afterDataFetched(entity, result, context);
    }

    @Override
    public PropertyReader reader(NestedResourceEntity<T> entity) {
        return null;
    }

    protected abstract void doOnParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context);

    protected abstract List<T> doOnParentDataResolved(
            NestedResourceEntity<T> entity,
            Iterable<?> parentData,
            SelectContext<?> context);
}
