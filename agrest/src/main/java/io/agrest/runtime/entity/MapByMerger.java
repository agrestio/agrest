package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.meta.AgAttribute;
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

        ResourceEntity<?> mapByRoot = toMapByEntity(entity);
        AgAttribute attribute = entity.getAgEntity().getAttribute(mapByPath);

        // TODO: this if/else may not be needed . we can use ResourceEntityTreeBuilder for both attributes and relationships...
        //   see "mergeIncluded" below
        if (attribute != null) {
            mapByRoot.getAttributes().put(attribute.getName(), attribute);
            entity.mapBy(mapByRoot, attribute.getName());
        } else {
            new ResourceEntityTreeBuilder(mapByRoot, metadataService::getAgEntity, overlays).inflatePath(mapByPath);
            entity.mapBy(mapByRoot, mapByPath);
        }
    }

    @Override
    public <T> void mergeIncluded(ResourceEntity<T> entity, String mapByPath, Map<Class<?>, AgEntityOverlay<?>> overlays) {
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
            new ResourceEntityTreeBuilder(mapByRoot, metadataService::getAgEntity, overlays).inflatePath(mapByPath);
            entity.mapBy(mapByRoot, mapByPath);
        } else {
            LOGGER.info("Ignoring 'mapBy:" + mapByPath + "' for to-one relationship property");
        }
    }

    private <T> ResourceEntity<T> toMapByEntity(ResourceEntity<T> entity) {
        return new ResourceEntity<>(entity.getAgEntity(), entity.getAgEntityOverlay());
    }
}
