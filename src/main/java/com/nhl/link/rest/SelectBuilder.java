package com.nhl.link.rest;

import java.util.Collection;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.annotation.listener.DataFetched;
import com.nhl.link.rest.annotation.listener.SelectChainInitialized;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.runtime.LinkRestBuilder;

/**
 * An object that allows to customize/extend LinkRest request processing.
 * SelectBuilder instance is bootstrapped by the framework based on a user query
 * or a root entity and then configured by the user server-side code to achieve
 * the desired behavior. Finally {@link #select()} is called to get the results.
 */
public interface SelectBuilder<T> {

	/**
	 * Sets request {@link UriInfo} that is a source of various request
	 * parameters.
	 * 
	 * @since 1.14
	 */
	SelectBuilder<T> uri(UriInfo uriInfo);

	/**
	 * Sets the encoder for the entities under the "data" key in the response
	 * collection.
	 * 
	 * @since 1.14
	 */
	SelectBuilder<T> dataEncoder(Encoder encoder);

	/**
	 * Configures SelectBuilder for a common scenario of "autocomplete" request,
	 * allowing the server-side code to choose which object property to use for
	 * selecting matching objects.
	 * 
	 * @since 1.14
	 */
	SelectBuilder<T> autocompleteOn(Property<?> autocompleteProperty);

	/**
	 * Forces the builder to select a single object by ID. Any explicit query
	 * associated with the builder is ignored (except possibly for the root
	 * entity resolution purposes). And a new ID query is built internally by
	 * LinkRest.
	 */
	SelectBuilder<T> byId(Object id);

	/**
	 * Adds a custom property that is appended to the root
	 * {@link ResourceEntity}.
	 * 
	 * @since 1.14
	 */
	SelectBuilder<T> property(String name, EntityProperty clientProperty);

	/**
	 * Adds a custom property that is appended to the root
	 * {@link ResourceEntity}. Property is read as a regular JavaBean
	 * "property", and default encoder is used. For more control over property
	 * access and encoding use {@link #property(String, EntityProperty)}. Also
	 * see {@link LinkRestBuilder#transientProperty(Class, String)}.
	 * 
	 * @since 1.14
	 */
	SelectBuilder<T> property(String name);

	/**
	 * Applies entity constraints to the SelectBuilder.
	 * 
	 * @since 1.3
	 */
	SelectBuilder<T> constraints(ConstraintsBuilder<T> constraints);

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
	 * Adds an annotated listener that will be notified of completion of
	 * individual stages during request processing. Recognized annotations are
	 * {@link SelectChainInitialized}, {@link SelectRequestParsed},
	 * {@link SelectServerParamsApplied}, {@link DataFetched}. Annotated
	 * method can take two forms, one that doesn't change the flow, and another
	 * one - that does:
	 * 
	 * <pre>
	 * void doSomething(SelectContext<?> context) {
	 * }
	 * 
	 * <T> ProcessingStage<SelectContext<T>, T> doSomethingWithTheFlow(SelectContext<T> context,
	 * 		ProcessingStage<SelectContext<T>, T> next) {
	 * }
	 * </pre>
	 * 
	 * @since 1.19
	 */
	SelectBuilder<T> listener(Object listener);

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
