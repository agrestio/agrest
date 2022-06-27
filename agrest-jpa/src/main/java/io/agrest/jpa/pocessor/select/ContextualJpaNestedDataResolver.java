package io.agrest.jpa.pocessor.select;

import io.agrest.AgException;
import io.agrest.NestedResourceEntity;
import io.agrest.jpa.pocessor.JpaProcessor;
import io.agrest.jpa.pocessor.JpaResourceEntityExt;
import io.agrest.processor.ProcessingContext;
import io.agrest.property.PropertyReader;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 5.0
 */
public class ContextualJpaNestedDataResolver<T> implements NestedDataResolver<T> {

    private final NestedDataResolver<T> parentQueryResolver;
    private final NestedDataResolver<T> parentIdsResolver;

    public ContextualJpaNestedDataResolver(
            NestedDataResolver<T> parentQueryResolver,
            NestedDataResolver<T> parentIdsResolver) {

        this.parentQueryResolver = parentQueryResolver;
        this.parentIdsResolver = parentIdsResolver;
    }

    @Override
    public void onParentQueryAssembled(NestedResourceEntity<T> entity, SelectContext<?> context) {
        pickResolver(entity).onParentQueryAssembled(entity, context);
    }

    @Override
    public void onParentDataResolved(NestedResourceEntity<T> entity, Iterable<?> parentData, SelectContext<?> context) {
        pickResolver(entity).onParentDataResolved(entity, parentData, context);
    }

    @Override
    public PropertyReader reader(NestedResourceEntity<T> entity, ProcessingContext<?> context) {
        return pickResolver(entity).reader(entity, context);
    }

    protected NestedDataResolver<T> pickResolver(NestedResourceEntity<T> entity) {
        JpaResourceEntityExt parentExt = JpaProcessor.getEntity(entity.getParent());

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

