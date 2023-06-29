package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.access.PathChecker;
import io.agrest.protocol.Include;
import io.agrest.runtime.meta.RequestSchema;

import java.util.List;

/**
 * @since 2.13
 */
public interface IIncludeMerger {

    /**
     * @since 5.0 takes RequestSchema parameter
     */
    void merge(ResourceEntity<?> resourceEntity, List<Include> includes, RequestSchema schema, PathChecker pathChecker);
}
