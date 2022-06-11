package io.agrest.resolver;

import io.agrest.RelatedResourceEntity;
import io.agrest.processor.ProcessingContext;
import io.agrest.reader.DataReader;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @param <T>
 * @since 3.4
 */
public class ReaderBasedResolver<T> extends BaseRelatedDataResolver<T> {

    private final DataReader reader;

    public ReaderBasedResolver(DataReader reader) {
        this.reader = reader;
    }

    @Override
    protected void doOnParentQueryAssembled(RelatedResourceEntity<T> entity, SelectContext<?> context) {
        // do nothing .. parent entity will query our data for us
    }

    @Override
    protected Iterable<T> doOnParentDataResolved(RelatedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        // do nothing .. parent entity will carry our data for us
        return iterableData(entity, parentData);
    }

    @Override
    public DataReader dataReader(RelatedResourceEntity<T> entity, ProcessingContext<?> context) {
        return reader;
    }

    protected Iterable<T> iterableData(RelatedResourceEntity<T> entity, Iterable<?> parentData) {
        return entity.getIncoming().isToMany()
                ? () -> new ToManyFlattenedIterator<>(parentData.iterator(), reader)
                : () -> new ToOneFlattenedIterator<>(parentData.iterator(), reader);
    }
}
