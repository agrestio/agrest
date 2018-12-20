package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.protocol.Limit;
import io.agrest.protocol.Start;

/**
 * @since 2.13
 */
public interface ISizeMerger {

    void merge(ResourceEntity<?, ?> resourceEntity, Start start, Limit limit);
}
