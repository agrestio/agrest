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

	Collection<LrAttribute> getAttributes();

	LrAttribute getAttribute(String name);

	Collection<LrRelationship> getRelationships();

	LrRelationship getRelationship(String name);
}
