package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;

/**
 * @since 2.13
 */
public interface IMapByMerger {

    void merge(ResourceEntity<?> resourceEntity, String mapByPath);

    void mergeIncluded(ResourceEntity<?> resourceEntity, String mapByPath);

}
