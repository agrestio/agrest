package io.agrest.resolver;

import io.agrest.NestedResourceEntity;
import io.agrest.processor.ProcessingContext;
import io.agrest.property.PropertyReader;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @param <T>
 * @since 3.4
 */
public class ReaderBasedResolver<T> extends BaseNestedDataResolver<T> {

    private final PropertyReader reader;

    public ReaderBasedResolver(PropertyReader reader) {
        this.reader = reader;
    }

    @Override
    protected void doOnParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
        // do nothing .. parent entity will query our data for us
    }

    @Override
    protected Iterable<T> doOnParentDataResolved(NestedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        // do nothing .. parent entity will carry our data for us
        return iterableData(entity, parentData);
    }

    @Override
    public PropertyReader reader(NestedResourceEntity<T> entity, ProcessingContext<?> context) {
        return reader;
    }

    protected Iterable<T> iterableData(NestedResourceEntity<T> entity, Iterable<?> parentData) {
        return entity.getIncoming().isToMany()
                ? () -> new ToManyFlattenedIterator<>(parentData.iterator(), reader)
                : () -> new ToOneFlattenedIterator<>(parentData.iterator(), reader);
    }
}
