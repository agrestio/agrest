package io.agrest.resolver;

import io.agrest.RelatedResourceEntity;
import io.agrest.processor.ProcessingContext;
import io.agrest.reader.DataReader;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @param <T>
 * @since 3.4
 */
public class ThrowingRelatedDataResolver<T> implements RelatedDataResolver<T> {

    private static final RelatedDataResolver instance = new ThrowingRelatedDataResolver();

    public static <T> RelatedDataResolver<T> getInstance() {
        return instance;
    }

    @Override
    public void onParentQueryAssembled(RelatedResourceEntity<T> entity, SelectContext<?> context) {
        throw new UnsupportedOperationException(
                "This is a placeholder for the related resolver. " +
                        "A real resolver is needed to read entity '" + entity.getName() + "'");
    }

    @Override
    public void onParentDataResolved(RelatedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        throw new UnsupportedOperationException(
                "This is a placeholder for the related resolver. " +
                        "A real resolver is needed to read entity '" + entity.getName() + "'");
    }

    @Override
    public DataReader dataReader(RelatedResourceEntity<T> entity, ProcessingContext<?> context) {
        throw new UnsupportedOperationException(
                "This is a placeholder for the related resolver. " +
                        "A real resolver is needed to read entity '" + entity.getName() + "'");
    }
}
