package com.nhl.link.rest.meta;

import java.util.Collection;

/**
 * A model of an entity.
 * 
 * @since 1.12
 */
public interface LrEntity<T> {

	String getName();

	Class<T> getType();

	Collection<LrAttribute> getIds();

	/**
	 * Returns ID attribute for a single ID entity. Throws if the entity has
	 * zero or more than one ID. This is a placeholder safety method until we
	 * start to fully support multi-attribute IDs.
	 */
	// TODO: we should strive to stop using this method internally, and
	// eventually get rid of it... multi-attribute IDs are expected after all.
	LrAttribute getSingleId();

	Collection<LrAttribute> getAttributes();

	LrAttribute getAttribute(String name);

	Collection<LrRelationship> getRelationships();

	LrRelationship getRelationship(String name);
}
