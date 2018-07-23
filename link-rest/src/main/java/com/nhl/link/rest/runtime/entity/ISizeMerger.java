package com.nhl.link.rest.runtime.entity;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.Start;

/**
 * @since 2.13
 */
public interface ISizeMerger {

    void construct(ResourceEntity<?> resourceEntity, Start start, Limit limit);
}
