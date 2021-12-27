package io.agrest.resolver;

import io.agrest.RootResourceEntity;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.List;

/**
 * A common superclass of hierarchical {@link RootDataResolver} implementations.
 *
 * @param <T>
 * @since 3.4
 */
public abstract class BaseRootDataResolver<T> extends BaseDataResolver implements RootDataResolver<T> {

    @Override
    public void assembleQuery(SelectContext<T> context) {
        doAssembleQuery(context);
        afterQueryAssembled(context.getEntity(), context);
    }

    @Override
    public void fetchData(SelectContext<T> context) {
        RootResourceEntity<T> entity = context.getEntity();
        List<T> result = doFetchData(context);
        entity.setResult(result);
        afterDataFetched(entity, result, context);
    }

    protected void doAssembleQuery(SelectContext<T> context) {
        // do nothing by default
    }

    protected abstract List<T> doFetchData(SelectContext<T> context);
}
