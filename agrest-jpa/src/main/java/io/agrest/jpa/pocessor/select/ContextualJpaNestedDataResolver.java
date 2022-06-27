package io.agrest.jpa.pocessor.select;

import io.agrest.AgException;
import io.agrest.RelatedResourceEntity;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.jpa.pocessor.JpaResourceEntityExt;
import io.agrest.processor.ProcessingContext;
import io.agrest.reader.DataReader;
import io.agrest.resolver.RelatedDataResolver;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 5.0
 */
public class ContextualJpaNestedDataResolver<T> implements RelatedDataResolver<T> {

    private final RelatedDataResolver<T> parentQueryResolver;
    private final RelatedDataResolver<T> parentIdsResolver;

    public ContextualJpaNestedDataResolver(
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
        JpaResourceEntityExt parentExt = JpaProcessor.getEntity(entity.getParent());

        // depending on the parent JPA semantics, we have some choices to make
        if (parentExt == null) {
            throw AgException.internalServerError(
                    "Parent entity '%s' of entity '%s' is not managed by the JPA backend",
                    entity.getParent().getName(),
                    entity.getName());
        }

        return parentExt.getSelect() != null ? parentQueryResolver : parentIdsResolver;
    }
}

