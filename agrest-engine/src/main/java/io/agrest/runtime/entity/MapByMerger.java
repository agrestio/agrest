package io.agrest.runtime.entity;

import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.ToManyResourceEntity;
import io.agrest.ToOneResourceEntity;
import io.agrest.access.MaxPathDepth;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgSchema;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @since 2.13
 */
public class MapByMerger implements IMapByMerger {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapByMerger.class);

    private final AgSchema schema;

    public MapByMerger(@Inject AgSchema schema) {
        this.schema = schema;
    }

    @Override
    public <T> void merge(ResourceEntity<T> entity, String mapByPath, Map<Class<?>, AgEntityOverlay<?>> overlays) {
        if (mapByPath == null) {
            return;
        }

        if (entity == null) {
            LOGGER.info("Ignoring 'mapBy : {}' for non-relationship property", mapByPath);
            return;
        }

        if (entity instanceof RelatedResourceEntity && !((RelatedResourceEntity) entity).getIncoming().isToMany()) {
            LOGGER.info("Ignoring 'mapBy : {}' for to-one relationship property", mapByPath);
            return;
        }

        ResourceEntity<?> mapByCompanionEntity = entity instanceof RelatedResourceEntity
                ? mapByCompanionEntity((RelatedResourceEntity) entity)
                : mapByCompanionEntity((RootResourceEntity) entity);

        new ResourceEntityTreeBuilder(mapByCompanionEntity, schema, overlays,
                // TODO: take the depth from the context
                MaxPathDepth.ofDefault().getDepth())
                .inflatePath(mapByPath);
        entity.mapBy(mapByCompanionEntity);
    }

    protected <T> RootResourceEntity<?> mapByCompanionEntity(RootResourceEntity<T> entity) {
        return new RootResourceEntity<>(entity.getAgEntity());
    }

    protected <T> RelatedResourceEntity<?> mapByCompanionEntity(RelatedResourceEntity<T> entity) {
        return entity instanceof ToOneResourceEntity
                ? new ToOneResourceEntity<>(entity.getAgEntity(), entity.getParent(), entity.getIncoming())
                : new ToManyResourceEntity<>(entity.getAgEntity(), entity.getParent(), entity.getIncoming());
    }
}
