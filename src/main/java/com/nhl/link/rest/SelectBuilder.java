package com.nhl.link.rest;

import java.util.Collection;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.encoder.Encoder;

/**
 * An object that allows to customize/extend LinkRest request processing.
 * SelectBuilder instance is bootstrapped by the framework based on a user query
 * or a root entity and then configured by the user server-side code to achieve
 * the desired behavior. Finally {@link #select()} is called to get the results.
 */
public interface SelectBuilder<T> {

	SelectBuilder<T> with(UriInfo uriInfo);

	SelectBuilder<T> withDataEncoder(Encoder encoder);

	SelectBuilder<T> withAutocompleteOn(Property<?> autocompleteProperty);

	/**
	 * Forces the builder to select a single object by ID. Any explicit query
	 * associated with the builder is ignored (except possibly for the root
	 * entity resolution purposes). And a new ID query is built internally by
	 * LinkRest.
	 */
	SelectBuilder<T> byId(Object id);

	/**
	 * Adds a custom property that is appended to the root ClientEntity.
	 */
	SelectBuilder<T> withProperty(String name, EntityProperty clientProperty);

	/**
	 * Adds a custom property that is appended to the root ClientEntity.
	 * Property is read as a regular JavaBean "property", and default encoder is
	 * used. For more control over property access and encoding use
	 * {@link #withProperty(String, EntityProperty)}.
	 */
	SelectBuilder<T> withProperty(String name);

	/**
	 * Applies entity constraints to the SelectBuilder.
	 * 
	 * @since 1.3
	 */
	SelectBuilder<T> constraints(TreeConstraints<T> constraints);

	/**
	 * @since 1.4
	 */
	SelectBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent);

	/**
	 * @since 1.4
	 */
	SelectBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent);

	/**
	 * @since 1.7
	 */
	SelectBuilder<T> parent(EntityParent<?> parent);

	/**
	 * @since 1.7
	 */
	SelectBuilder<T> toManyParent(Class<?> parentType, Object parentId,
			Property<? extends Collection<T>> relationshipFromParent);

	/**
	 * @since 1.2
	 */
	SelectBuilder<T> fetchOffset(int offset);

	/**
	 * @since 1.2
	 */
	SelectBuilder<T> fetchLimit(int limit);

	/**
	 * Runs the query corresponding to the state of this builder, returning
	 * response that can be serialized by the framework.
	 */
	DataResponse<T> select();

	/**
	 * Runs the query corresponding to the state of this builder, returning
	 * response that can be serialized by the framework. The difference with
	 * {@link #select()} is that the framework ensures that one and only one
	 * record is returned in response. Zero records result in 404 response, 2 or
	 * more records - in 500 response.
	 * <p>
	 * Note that "by id" selects are routing to "selectOne" internally even if
	 * query is invoked as "select". This is for backwards compatibility with
	 * 1.1. Should change that eventuall.y
	 * 
	 * @since 1.2
	 */
	DataResponse<T> selectOne();
}
