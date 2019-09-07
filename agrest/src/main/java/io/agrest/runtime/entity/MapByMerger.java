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
    public void merge(ResourceEntity<?> resourceEntity, String mapByPath) {
        if (mapByPath == null) {
            return;
        }

        if (mapByPath != null) {
            AgAttribute attribute = resourceEntity.getAgEntity().getAttribute(mapByPath);
            if (attribute != null) {
                ResourceEntity<?> mapByEntity = new ResourceEntity<>(resourceEntity.getAgEntity());
                mapByEntity.getAttributes().put(attribute.getName(), attribute);
                resourceEntity.mapBy(mapByEntity, attribute.getName());
            } else {
                ResourceEntity<?> mapByEntity = new ResourceEntity<>(resourceEntity.getAgEntity());
                IncludeMerger.checkTooLong(mapByPath);

                // TODO: Non-phantom entity tracking HashSet is not really used here... Should we unwind it from include path
                //  processing somehow? (an option is to track it inside ResourceEntity seems dirty)
                IncludeMerger.processIncludePath(mapByEntity, mapByPath, new HashSet<>());
                resourceEntity.mapBy(mapByEntity, mapByPath);
            }
        }

    }

    @Override
    public void mergeIncluded(ResourceEntity<?> resourceEntity, String mapByPath) {
        if (mapByPath == null) {
            return;
        }

        if (resourceEntity == null) {
            LOGGER.info("Ignoring 'mapBy:{}' for non-relationship property", mapByPath);
            return;
        }

        // either root list, or to-many relationship
        if (resourceEntity.getIncoming() == null || resourceEntity.getIncoming().isToMany()) {

            ResourceEntity<?> mapByRoot = new ResourceEntity<>(resourceEntity.getAgEntity());
            IncludeMerger.checkTooLong(mapByPath);
            // TODO: Non-phantom entity tracking HashSet is not really used here... Should we unwind it from include path
            //  processing somehow? (an option is to track it inside ResourceEntity seems dirty)
            IncludeMerger.processIncludePath(mapByRoot, mapByPath, new HashSet<>());
            resourceEntity.mapBy(mapByRoot, mapByPath);

        } else {
            LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for to-one relationship property");
        }
    }
}
