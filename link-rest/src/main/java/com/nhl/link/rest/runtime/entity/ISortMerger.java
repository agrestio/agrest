package com.nhl.link.rest.runtime.entity;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.protocol.Sort;

/**
 * @since 2.13
 */
public interface ISortMerger {

    void construct(ResourceEntity<?> entity, Sort sort);
}
