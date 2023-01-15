package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.access.MaxPathDepth;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.protocol.Include;

import java.util.List;
import java.util.Map;

/**
 * @since 2.13
 */
public interface IIncludeMerger {

    /**
     * @since 3.4 additionally takes request overlays
     */
    void merge(ResourceEntity<?> resourceEntity, List<Include> includes, Map<Class<?>, AgEntityOverlay<?>> overlays, MaxPathDepth maxPathDepth);
}
