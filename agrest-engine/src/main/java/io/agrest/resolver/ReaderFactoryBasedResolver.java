package io.agrest.resolver;

import io.agrest.RelatedResourceEntity;
import io.agrest.processor.ProcessingContext;
import io.agrest.reader.DataReader;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.function.Function;

/**
 * @param <T>
 * @since 3.4
 */
public class ReaderFactoryBasedResolver<T> extends BaseRelatedDataResolver<T> {

    private final Function<RelatedResourceEntity<T>, DataReader> readerFactory;

    public ReaderFactoryBasedResolver(Function<RelatedResourceEntity<T>, DataReader> readerFactory) {
        this.readerFactory = readerFactory;
    }

    @Override
    protected void doOnParentQueryAssembled(RelatedResourceEntity<T> entity, SelectContext<?> context) {
        // do nothing .. parent entity will query our data for us
    }


    @Override
    protected Iterable<T> doOnParentDataResolved(RelatedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        // do nothing .. parent entity will carry our data for us
        return iterableData(entity, parentData, context);
    }

    @Override
    public DataReader dataReader(RelatedResourceEntity<T> entity, ProcessingContext<?> context) {
        return readerFactory.apply(entity);
    }

    protected Iterable<T> iterableData(RelatedResourceEntity<T> entity, Iterable<?> parentData, ProcessingContext<?> context) {
        DataReader reader = this.dataReader(entity, context);
        return entity.getIncoming().isToMany()
                ? () -> new ToManyFlattenedIterator<>(parentData.iterator(), reader)
                : () -> new ToOneFlattenedIterator<>(parentData.iterator(), reader);
    }
}
