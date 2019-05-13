package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;

public class SizeMerger implements ISizeMerger {

    @Override
    public void merge(ResourceEntity<?> resourceEntity, Integer start, Integer limit) {
        if (start != null) {
            resourceEntity.setFetchOffset(start);
        }
        if (limit != null) {
            resourceEntity.setFetchLimit(limit);
        }
    }
}
