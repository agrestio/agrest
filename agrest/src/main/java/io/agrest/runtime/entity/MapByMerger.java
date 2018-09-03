package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.meta.LrAttribute;
import io.agrest.protocol.MapBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 2.13
 */
public class MapByMerger implements IMapByMerger {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapByMerger.class);

    @Override
    public void merge(ResourceEntity<?> resourceEntity, MapBy mapBy) {
        if (mapBy == null) {
            return;
        }

        final String mapByPath = mapBy.getPath();
        if (mapByPath != null) {
            LrAttribute attribute = resourceEntity.getLrEntity().getAttribute(mapByPath);
            if (attribute != null) {
                ResourceEntity<?> mapByEntity = new ResourceEntity<>(resourceEntity.getLrEntity());
                mapByEntity.getAttributes().put(attribute.getName(), attribute);
                resourceEntity.mapBy(mapByEntity, attribute.getName());
            } else {
                ResourceEntity<?> mapByEntity = new ResourceEntity<>(resourceEntity.getLrEntity());
                IncludeMerger.checkTooLong(mapByPath);
                IncludeMerger.processIncludePath(mapByEntity, mapByPath);
                resourceEntity.mapBy(mapByEntity, mapByPath);
            }
        }

    }

    @Override
    public void mergeIncluded(ResourceEntity<?> resourceEntity, MapBy mapBy) {
        if (mapBy == null) {
            return;
        }

        final String mapByPath = mapBy.getPath();
        if (resourceEntity == null) {
            LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for non-relationship property");
            return;
        }

        // either root list, or to-many relationship
        if (resourceEntity.getIncoming() == null || resourceEntity.getIncoming().isToMany()) {

            ResourceEntity<?> mapByRoot = new ResourceEntity<>(resourceEntity.getLrEntity());
            IncludeMerger.checkTooLong(mapByPath);
            IncludeMerger.processIncludePath(mapByRoot, mapByPath);
            resourceEntity.mapBy(mapByRoot, mapByPath);

        } else {
            LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for to-one relationship property");
        }
    }
}
