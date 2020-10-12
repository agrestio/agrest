package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.base.protocol.CayenneExp;

/**
 * @since 2.13
 */
public class CayenneExpMerger implements ICayenneExpMerger {

    @Override
    public void merge(ResourceEntity<?> resourceEntity, CayenneExp cayenneExp) {
        if (cayenneExp != null) {
            resourceEntity.getQualifiers().add(cayenneExp);
        }
    }
}
