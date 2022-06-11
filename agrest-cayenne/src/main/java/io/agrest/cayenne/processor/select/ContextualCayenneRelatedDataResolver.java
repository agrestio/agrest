package io.agrest.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.RelatedResourceEntity;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.cayenne.processor.CayenneResourceEntityExt;
import io.agrest.processor.ProcessingContext;
import io.agrest.reader.DataReader;
import io.agrest.resolver.RelatedDataResolver;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 4.8
 */
public class ContextualCayenneRelatedDataResolver<T> implements RelatedDataResolver<T> {

    private final RelatedDataResolver<T> parentQueryResolver;
    private final RelatedDataResolver<T> parentIdsResolver;

    public ContextualCayenneRelatedDataResolver(
            RelatedDataResolver<T> parentQueryResolver,
            RelatedDataResolver<T> parentIdsResolver) {

        this.parentQueryResolver = parentQueryResolver;
        this.parentIdsResolver = parentIdsResolver;
    }

    @Override
    public void onParentQueryAssembled(RelatedResourceEntity<T> entity, SelectContext<?> context) {
        pickResolver(entity).onParentQueryAssembled(entity, context);
    }

    @Override
    public void onParentDataResolved(RelatedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        pickResolver(entity).onParentDataResolved(entity, parentData, context);
    }

    @Override
    public DataReader dataReader(RelatedResourceEntity<T> entity, ProcessingContext<?> context) {
        return pickResolver(entity).dataReader(entity, context);
    }

    protected RelatedDataResolver<T> pickResolver(RelatedResourceEntity<T> entity) {
        CayenneResourceEntityExt parentExt = CayenneProcessor.getEntity(entity.getParent());

        // depending on the parent Cayenne semantics, we have some choices to make
        if (parentExt == null) {
            throw AgException.internalServerError(
                    "Parent entity '%s' of entity '%s' is not managed by the Cayenne backend",
                    entity.getParent().getName(),
                    entity.getName());
        }

        return parentExt.getSelect() != null ? parentQueryResolver : parentIdsResolver;
    }
}

