package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.meta.AgAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * @since 2.13
 */
public class MapByMerger implements IMapByMerger {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapByMerger.class);

    @Override
    public <T> void merge(ResourceEntity<T> entity, String mapByPath) {
        if (mapByPath == null) {
            return;
        }

        if (mapByPath != null) {
            ResourceEntity<?> mapByEntity = toMapByEntity(entity);

            AgAttribute attribute = entity.getAgEntity().getAttribute(mapByPath);
            if (attribute != null) {
                mapByEntity.getAttributes().put(attribute.getName(), attribute);
                entity.mapBy(mapByEntity, attribute.getName());
            } else {
                IncludeMerger.checkTooLong(mapByPath);

                // TODO: Non-phantom entity tracking HashSet is not really used here... Should we unwind it from include path
                //  processing somehow? (an option is to track it inside ResourceEntity seems dirty)
                IncludeMerger.processIncludePath(mapByEntity, mapByPath, new HashSet<>());
                entity.mapBy(mapByEntity, mapByPath);
            }
        }
    }

    @Override
    public <T> void mergeIncluded(ResourceEntity<T> entity, String mapByPath) {
        if (mapByPath == null) {
            return;
        }

        if (entity == null) {
            LOGGER.info("Ignoring 'mapBy:{}' for non-relationship property", mapByPath);
            return;
        }

        // either root list, or to-many relationship
        if (entity.getIncoming() == null || entity.getIncoming().isToMany()) {

            ResourceEntity<?> mapByRoot = toMapByEntity(entity);

            IncludeMerger.checkTooLong(mapByPath);
            // TODO: Non-phantom entity tracking HashSet is not really used here... Should we unwind it from include path
            //  processing somehow? (an option is to track it inside ResourceEntity seems dirty)
            IncludeMerger.processIncludePath(mapByRoot, mapByPath, new HashSet<>());
            entity.mapBy(mapByRoot, mapByPath);

        } else {
            LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for to-one relationship property");
        }
    }

    private <T> ResourceEntity<T> toMapByEntity(ResourceEntity<T> entity) {
        return new ResourceEntity<>(entity.getAgEntity(), entity.getAgEntityOverlay());
    }
}
