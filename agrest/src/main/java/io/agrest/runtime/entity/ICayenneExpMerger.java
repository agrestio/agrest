package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.protocol.CayenneExp;

/**
 * @since 2.13
 */
public interface ICayenneExpMerger {

	void merge(ResourceEntity<?> resourceEntity, CayenneExp cayenneExp);
}
