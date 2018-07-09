package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 2.13
 */
public class MapByProcessor implements IMapByProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MapByProcessor.class);

    @Override
    public void processInclude(ResourceEntity<?> resourceEntity, String mapByPath) {
        if (resourceEntity == null) {
            LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for non-relationship property");
            return;
        }

        // either root list, or to-many relationship
        if (resourceEntity.getIncoming() == null || resourceEntity.getIncoming().isToMany()) {

            ResourceEntity<?> mapByRoot = new ResourceEntity<>(resourceEntity.getLrEntity());
            BaseRequestProcessor.processIncludePath(mapByRoot, mapByPath);
            resourceEntity.mapBy(mapByRoot, mapByPath);

        } else {
            LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for to-one relationship property");
        }
    }

    @Override
    public void process(ResourceEntity<?> resourceEntity, String mapByPath) {
        if (mapByPath != null) {
            LrAttribute attribute = resourceEntity.getLrEntity().getAttribute(mapByPath);
            if (attribute != null) {
                ResourceEntity<?> mapBy = new ResourceEntity<>(resourceEntity.getLrEntity());
                mapBy.getAttributes().put(attribute.getName(), attribute);
                resourceEntity.mapBy(mapBy, attribute.getName());
            } else {
                ResourceEntity<?> mapBy = new ResourceEntity<>(resourceEntity.getLrEntity());
                BaseRequestProcessor.processIncludePath(mapBy, mapByPath);
                resourceEntity.mapBy(mapBy, mapByPath);
            }
        }
    }
}
