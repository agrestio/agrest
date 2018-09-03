package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.protocol.Limit;
import io.agrest.protocol.Start;

public class SizeMerger implements ISizeMerger {

    @Override
    public void merge(ResourceEntity<?> resourceEntity, Start start, Limit limit) {
        if (start != null) {
            resourceEntity.setFetchOffset(start.getValue());
        }
        if (limit != null) {
            resourceEntity.setFetchLimit(limit.getValue());
        }
    }
}
