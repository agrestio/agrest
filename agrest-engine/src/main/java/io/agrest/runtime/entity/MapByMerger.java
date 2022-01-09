package io.agrest.runtime.entity;

import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntityOverlay;
import org.apache.cayenne.di.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @since 2.13
 */
public class MapByMerger implements IMapByMerger {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapByMerger.class);

    private AgDataMap dataMap;

    public MapByMerger(@Inject AgDataMap dataMap) {
        this.dataMap = dataMap;
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

        if (entity instanceof NestedResourceEntity && !((NestedResourceEntity) entity).getIncoming().isToMany()) {
            LOGGER.info("Ignoring 'mapBy : {}' for to-one relationship property", mapByPath);
            return;
        }

        ResourceEntity<?> mapByCompanionEntity = new RootResourceEntity<>(entity.getAgEntity());
        new ResourceEntityTreeBuilder(mapByCompanionEntity, dataMap, overlays).inflatePath(mapByPath);
        entity.mapBy(mapByCompanionEntity, mapByPath);
    }
}
