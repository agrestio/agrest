package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.base.protocol.CayenneExp;

/**
 * @since 2.13
 */
public interface ICayenneExpMerger {

	void merge(ResourceEntity<?> resourceEntity, CayenneExp cayenneExp);
}
