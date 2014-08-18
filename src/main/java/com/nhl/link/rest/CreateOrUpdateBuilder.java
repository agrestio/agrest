package com.nhl.link.rest;

import java.util.Collection;

import org.apache.cayenne.exp.Property;

/**
 * A builder for create (insert) or update operations for a single entity type.
 * The builder has a number of flavors of the resulting operation that are
 * invoked after configuring the options : {@link #create()}, {@link #update()}
 * {@link #createOrUpdate()}, {@link #createOrUpdateIdempotent()}.
 * 
 * @since 1.3
 */
public interface CreateOrUpdateBuilder<T> {

	// TODO:
	// CreateOrUpdateBuilder<T> responseEncoder(Encoder encoder);

	/**
	 * Set an explicit id for the update. In this case only a single object is
	 * allowed in the update.
	 */
	CreateOrUpdateBuilder<T> id(Object id);

	/**
	 * Sets up a relationship clause for all objects in this update.
	 */
	CreateOrUpdateBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent);

	/**
	 * Sets up a relationship clause for all objects in this update.
	 */
	CreateOrUpdateBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent);

	/**
	 * Sets up a relationship clause for all objects in this update.
	 */
	CreateOrUpdateBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent);

	UpdateResponse<T> process(String entityData);
}
