package io.agrest.meta;

import java.util.Collection;

/**
 * A model of an entity.
 * 
 * @since 1.12
 */
public interface AgEntity<T> {

	String getName();

	Class<T> getType();

	Collection<AgAttribute> getIds();

	Collection<AgAttribute> getAttributes();

	AgAttribute getAttribute(String name);

	Collection<AgRelationship> getRelationships();

	AgRelationship getRelationship(String name);

	AgRelationship getRelationship(AgEntity entity);
}
