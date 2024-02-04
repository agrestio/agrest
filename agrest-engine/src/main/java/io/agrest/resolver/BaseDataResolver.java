package io.agrest.resolver;

import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.runtime.processor.select.SelectContext;

/**
 * @since 3.4
 */
public abstract class BaseDataResolver {

    protected void afterQueryAssembled(ResourceEntity<?> entity, SelectContext<?> context) {

        ResourceEntity<?> mapBy = entity.getMapBy();
        if (mapBy != null) {
            for (RelatedResourceEntity c : mapBy.getChildren()) {
                c.getResolver().onParentQueryAssembled(c, context);
            }
        }

        for (RelatedResourceEntity c : entity.getChildren()) {
            c.getResolver().onParentQueryAssembled(c, context);
        }
    }

    protected <T> void afterDataFetched(ResourceEntity<T> entity, Iterable<T> data, SelectContext<?> context) {

        ResourceEntity<?> mapBy = entity.getMapBy();
        if (mapBy != null) {
            for (RelatedResourceEntity c : mapBy.getChildren()) {
                c.getResolver().onParentDataResolved(c, data, context);
            }
        }

        for (RelatedResourceEntity c : entity.getChildren()) {
            c.getResolver().onParentDataResolved(c, data, context);
        }
    }
}
