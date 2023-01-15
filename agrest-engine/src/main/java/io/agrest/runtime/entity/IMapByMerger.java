package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.access.PathChecker;
import io.agrest.meta.AgEntityOverlay;

import java.util.Map;

/**
 * @since 2.13
 */
public interface IMapByMerger {
    
    /**
     * @since 3.4 additionally takes request overlays
     */
    <T> void merge(ResourceEntity<T> entity, String mapByPath, Map<Class<?>, AgEntityOverlay<?>> overlays, PathChecker pathChecker);
}
