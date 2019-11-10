package io.agrest.resolver;

import io.agrest.NestedResourceEntity;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.Collections;
import java.util.function.Function;

/**
 * @param <T>
 * @since 3.4
 */
public class ReaderFactoryBasedResolver<T> extends BaseNestedDataResolver<T> {

    private Function<NestedResourceEntity<T>, PropertyReader> readerFactory;

    public ReaderFactoryBasedResolver(Function<NestedResourceEntity<T>, PropertyReader> readerFactory) {
        this.readerFactory = readerFactory;
    }

    @Override
    protected void doOnParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
        // do nothing .. parent entity will query our data for us
    }


    @Override
    protected Iterable<T> doOnParentDataResolved(NestedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        // do nothing .. parent entity will carry our data for us

        // TODO: create an iterable<T> for the sake of children
        return Collections.emptyList();
    }

    @Override
    public PropertyReader reader(NestedResourceEntity<T> entity) {
        return readerFactory.apply(entity);
    }
}
