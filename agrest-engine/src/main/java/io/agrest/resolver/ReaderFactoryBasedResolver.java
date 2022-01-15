package io.agrest.resolver;

import io.agrest.NestedResourceEntity;
import io.agrest.processor.ProcessingContext;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.function.Function;

/**
 * @param <T>
 * @since 3.4
 */
public class ReaderFactoryBasedResolver<T> extends BaseNestedDataResolver<T> {

    private final Function<NestedResourceEntity<T>, PropertyReader> readerFactory;

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
        return iterableData(entity, parentData, context);
    }

    @Override
    public PropertyReader reader(NestedResourceEntity<T> entity, ProcessingContext<?> context) {
        return readerFactory.apply(entity);
    }

    protected Iterable<T> iterableData(NestedResourceEntity<T> entity, Iterable<?> parentData, ProcessingContext<?> context) {
        PropertyReader reader = reader(entity, context);
        return entity.getIncoming().isToMany()
                ? () -> new ToManyFlattenedIterator<>(parentData.iterator(), reader)
                : () -> new ToOneFlattenedIterator<>(parentData.iterator(), reader);
    }
}
