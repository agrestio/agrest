package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.protocol.Sort;

/**
 * @since 2.13
 */
public interface ISortMerger {

    void merge(ResourceEntity<?, ?> entity, Sort sort);
}
