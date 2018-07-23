package com.nhl.link.rest.runtime.entity;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.Start;

public class SizeMerger implements ISizeMerger {

    @Override
    public void construct(ResourceEntity<?> resourceEntity, Start start, Limit limit) {
        if (start != null) {
            resourceEntity.setFetchOffset(start.getValue());
        }
        if (limit != null) {
            resourceEntity.setFetchLimit(limit.getValue());
        }
    }
}
