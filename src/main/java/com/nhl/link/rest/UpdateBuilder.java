package com.nhl.link.rest;

import java.util.Collection;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;

/**
 * A builder for create (insert) or update operations for a single entity type.
 * The builder has a number of flavors of the resulting operation that are
 * invoked after configuring the options : {@link #create()}, {@link #update()}
 * {@link #createOrUpdate()}, {@link #createOrUpdateIdempotent()}.
 * 
 * @since 1.7
 */
public interface UpdateBuilder<T> {

	// TODO:
	// CreateOrUpdateBuilder<T> responseEncoder(Encoder encoder);

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
	 */
	UpdateBuilder<T> with(UriInfo uriInfo);

	UpdateBuilder<T> readConstraints(TreeConstraints<T> constraints);

	UpdateBuilder<T> writeConstraints(TreeConstraints<T> constraints);

	/**
	 * Sets a custom mapper that locates existing objects based on request data.
	 * If not set, objects will be located by their ID.
	 * 
	 * @since 1.4
	 */
	UpdateBuilder<T> mapper(ObjectMapper mapper);

	UpdateResponse<T> process(String entityData);
}
