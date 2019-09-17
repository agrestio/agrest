package io.agrest.runtime.entity;

import io.agrest.ChildResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.runtime.meta.IMetadataService;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @since 2.13
 */
public class MapByMerger implements IMapByMerger {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapByMerger.class);

    private IMetadataService metadataService;

    public MapByMerger(@Inject IMetadataService metadataService) {
        this.metadataService = metadataService;
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

        if (entity instanceof ChildResourceEntity && !((ChildResourceEntity) entity).getIncoming().isToMany()) {
            LOGGER.info("Ignoring 'mapBy : {}' for to-one relationship property", mapByPath);
            return;
        }

        ResourceEntity<?> mapByCompanionEntity = new RootResourceEntity<>(entity.getAgEntity(), entity.getAgEntityOverlay());
        new ResourceEntityTreeBuilder(mapByCompanionEntity, metadataService::getAgEntity, overlays).inflatePath(mapByPath);
        entity.mapBy(mapByCompanionEntity, mapByPath);
    }
}
