package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.protocol.Exclude;

import java.util.List;

/**
 * @since 2.13
 */
public interface IExcludeMerger {

    void merge(ResourceEntity<?, ?> resourceEntity, List<Exclude> excludes);
}
