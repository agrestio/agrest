package com.nhl.link.rest.runtime.entity;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.protocol.CayenneExp;

/**
 * @since 2.13
 */
public interface ICayenneExpMerger {

	void merge(ResourceEntity<?> resourceEntity, CayenneExp cayenneExp);
}
