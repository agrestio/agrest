package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.meta.AgEntityOverlay;

import java.util.Map;

/**
 * @since 2.13
 */
public interface IMapByMerger {

    // TODO: "merge" and "mergeIncluded" seem to do the same thing. Why 2 methods?

    /**
     * @since 3.4 additionally takes request overlays
     */
    <T> void merge(ResourceEntity<T> entity, String mapByPath, Map<String, AgEntityOverlay<?>> overlays);

    /**
     * @since 3.4 additionally takes request overlays
     */
    <T> void mergeIncluded(
            ResourceEntity<T> childEntity,
            String incomingMapByPath,
            Map<String, AgEntityOverlay<?>> requestOverlays);
}
