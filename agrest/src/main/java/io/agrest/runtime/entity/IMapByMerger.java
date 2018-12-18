package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.protocol.MapBy;

/**
 * @since 2.13
 */
public interface IMapByMerger {

    void merge(ResourceEntity<?, ?> resourceEntity, MapBy mapBy);

    void mergeIncluded(ResourceEntity<?, ?> resourceEntity, MapBy mapBy);

}
