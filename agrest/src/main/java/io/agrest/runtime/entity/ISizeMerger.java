package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;

/**
 * @since 2.13
 */
public interface ISizeMerger {

    void merge(ResourceEntity<?> resourceEntity, Integer start, Integer limit);
}
