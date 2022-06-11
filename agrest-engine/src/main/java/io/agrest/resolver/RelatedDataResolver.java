package io.agrest.resolver;

import io.agrest.RelatedResourceEntity;
import io.agrest.processor.ProcessingContext;
import io.agrest.reader.DataReader;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 5.0
 */
public interface RelatedDataResolver<T> {

    void onParentQueryAssembled(RelatedResourceEntity<T> entity, SelectContext<?> context);

    void onParentDataResolved(RelatedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context);

    DataReader dataReader(RelatedResourceEntity<T> entity, ProcessingContext<?> context);
}
