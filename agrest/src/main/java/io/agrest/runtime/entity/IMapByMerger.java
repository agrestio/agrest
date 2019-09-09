package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;

/**
 * @since 2.13
 */
public interface IMapByMerger {

    <T> void merge(ResourceEntity<T> entity, String mapByPath);

    <T> void mergeIncluded(ResourceEntity<T> entity, String mapByPath);

}
