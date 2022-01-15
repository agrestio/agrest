package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;

public class SizeMerger implements ISizeMerger {

    @Override
    public void merge(ResourceEntity<?> resourceEntity, Integer start, Integer limit) {
        if (start != null) {
            resourceEntity.setStart(start);
        }
        if (limit != null) {
            resourceEntity.setLimit(limit);
        }
    }
}
