package com.nhl.link.rest;

import java.util.Collection;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.constraints.ConstraintsBuilder;

/**
 * A builder for create (insert) or update operations for a single entity type.
 * Depending on how the builder was created, it will performs one of the flavors
 * of update: create, update, createOrUpdate, fullSync.
 * 
 * @since 1.7
 */
public interface UpdateBuilder<T> {

	/**
	 * Set an explicit id for the update. In this case only a single object is
	 * allowed in the update.
	 */
	UpdateBuilder<T> id(Object id);

	/**
	 * Sets up a relationship clause for all objects in this update.
	 */
	UpdateBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent);

	/**
	 * Sets up a relationship clause for all objects in this update.
	 */
	UpdateBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent);

	/**
	 * Sets up a relationship clause for all objects in this update.
	 */
	UpdateBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent);

	/**
	 * Sets request {@link UriInfo} that will be used to shape response.
	 * 
	 * @since 1.14
	 */
	UpdateBuilder<T> uri(UriInfo uriInfo);

	UpdateBuilder<T> readConstraints(ConstraintsBuilder<T> constraints);

	UpdateBuilder<T> writeConstraints(ConstraintsBuilder<T> constraints);

	/**
	 * Sets a custom mapper that locates existing objects based on request data.
	 * If not set, objects will be located by their ID.
	 */
	UpdateBuilder<T> mapper(ObjectMapperFactory mapper);

	/**
	 * Results in a 'data' key included in response with the updated state of
	 * the collection.
	 */
	UpdateBuilder<T> includeData();

	/**
	 * Suppresses 'data' key in response.
	 */
	UpdateBuilder<T> excludeData();

	UpdateResponse<T> process(String entityData);
}
