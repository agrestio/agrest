package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.protocol.Include;

import java.util.List;

/**
 * @since 2.13
 */
public interface IIncludeMerger {

    void merge(ResourceEntity<?> resourceEntity, List<Include> includes);
}
