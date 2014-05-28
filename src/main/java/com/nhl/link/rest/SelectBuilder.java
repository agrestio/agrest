package com.nhl.link.rest;

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
	SelectBuilder<T> withProperty(String name, ClientProperty clientProperty);

	/**
	 * Adds a custom property that is appended to the root ClientEntity.
	 * Property is read as a regular JavaBean "property", and default encoder is
	 * used. For more control over property access and encoding use
	 * {@link #withProperty(String, ClientProperty)}.
	 */
	SelectBuilder<T> withProperty(String name);

	/**
	 * Runs the query corresponding to the state of this builder, returning
	 * response that can be serialized by the framework.
	 */
	DataResponse<T> select();
}
