package com.nhl.link.rest.runtime.entity;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.protocol.MapBy;

/**
 * @since 2.13
 */
public interface IMapByMerger {

    void construct(ResourceEntity<?> resourceEntity, MapBy mapBy);

    void constructIncluded(ResourceEntity<?> resourceEntity, MapBy mapBy);

}
