package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.protocol.Exp;

/**
 * @since 2.13
 */
public interface IExpMerger {

	void merge(ResourceEntity<?> resourceEntity, Exp exp);
}
