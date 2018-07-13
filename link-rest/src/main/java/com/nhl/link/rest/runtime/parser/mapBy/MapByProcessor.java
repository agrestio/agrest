package com.nhl.link.rest.runtime.parser.mapBy;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.query.MapBy;
import com.nhl.link.rest.runtime.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 2.13
 */
public class MapByProcessor implements IMapByProcessor {

    private MapByConverter converter;

    private static final Logger LOGGER = LoggerFactory.getLogger(MapByProcessor.class);

    public MapByProcessor() {
        this.converter = new MapByConverter();
    }

    @Override
    public void process(ResourceEntity<?> resourceEntity, String mapByPath) {
        if (mapByPath == null || mapByPath.isEmpty()) {
            return;
        }

        MapBy mapBy = converter.fromString(mapByPath);
        process(resourceEntity, mapBy);
    }

    @Override
    public void processIncluded(ResourceEntity<?> resourceEntity, MapBy mapBy) {
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
            MapByConverter.checkTooLong(mapByPath);
            BaseRequestProcessor.processIncludePath(mapByRoot, mapByPath);
            resourceEntity.mapBy(mapByRoot, mapByPath);

        } else {
            LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for to-one relationship property");
        }
    }

    @Override
    public void process(ResourceEntity<?> resourceEntity, Query query) {
        process(resourceEntity, query.getMapBy());
    }

    private void process(ResourceEntity<?> resourceEntity, MapBy mapBy) {
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
                MapByConverter.checkTooLong(mapByPath);
                BaseRequestProcessor.processIncludePath(mapByEntity, mapByPath);
                resourceEntity.mapBy(mapByEntity, mapByPath);
            }
        }
    }

    @Override
    public MapByConverter getConverter() {
        return this.converter;
    }
}
