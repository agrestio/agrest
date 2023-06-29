package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.access.PathChecker;
import io.agrest.runtime.meta.RequestSchema;

/**
 * @since 2.13
 */
public interface IMapByMerger {
    
    /**
     * @since 5.0 takes RequestSchema
     */
    <T> void merge(ResourceEntity<T> entity, String mapByPath, RequestSchema schema, PathChecker pathChecker);
}
