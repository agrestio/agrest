package com.nhl.link.rest;

import com.nhl.link.rest.annotation.listener.DataFetched;
import com.nhl.link.rest.annotation.listener.SelectChainInitialized;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.runtime.LinkRestBuilder;
import org.apache.cayenne.exp.Property;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Map;

/**
 * An object that allows to customize/extend LinkRest request processing.
 * SelectBuilder instance is bootstrapped by the framework based on a user query
 * or a root entity and then configured by the user server-side code to achieve
 * the desired behavior. Finally {@link #get()} is called to get the results.
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
	 * Forces the builder to select a single object by ID.
	 */
	SelectBuilder<T> byId(Object id);

	/**
	 * Forces the builder to select a single object by compound ID.
	 *
	 * @since 1.20
	 */
	SelectBuilder<T> byId(Map<String, Object> ids);

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
	 * Installs an optional constraint function defining how much of the request entity attributes / relationships
     * the client can see.
	 *
	 * @since 1.3
	 * @deprecated since 2.4 in favor of {@link #constraint(Constraint)}.
	 */
	default SelectBuilder<T> constraints(Constraint<T> constraint) {
	    return constraint(constraint);
    }

    /**
     * Installs an optional constraint function defining how much of the request entity attributes / relationships
     * the client can see.
     *
     * @param constraint an instance of Constraint function.
     * @return this builder instance.
     * @since 2.4
     */
    SelectBuilder<T> constraint(Constraint<T> constraint);

	/**
	 * @since 1.4
	 */
	SelectBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent);

	/**
	 * @since 1.20
	 */
	SelectBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent);

	/**
	 * @since 1.4
	 */
	SelectBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent);

	/**
	 * @since 1.20
	 */
	SelectBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, Property<T> relationshipFromParent);

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
	 * @since 1.20
	 */
	SelectBuilder<T> toManyParent(Class<?> parentType, Map<String, Object> parentIds,
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
	 * {@link SelectServerParamsApplied}, {@link DataFetched}. Annotated method
	 * can take two forms, one that doesn't change the flow, and another one -
	 * that does:
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
	 * a response that can be serialized by the framework.
	 *
	 * @since 2.4
	 */
	DataResponse<T> get();

	/**
	 * @deprecated since 2.4 in favor of {@link #get()}.
	 */
	default DataResponse<T> select() {
		return get();
	}

	/**
	 * Runs the query corresponding to the state of this builder, returning
	 * response that can be serialized by the framework. The difference with
	 * {@link #get()} is that the framework ensures that one and only one
	 * record is returned in response. Zero records result in 404 response, 2 or
	 * more records - in 500 response.
	 * <p>
	 * Note that "by id" selects are routing to "selectOne" internally even if
	 * query is invoked as "select". This is for backwards compatibility with
	 * 1.1. Should change that eventually.
	 *
	 * @since 1.2
	 */
	DataResponse<T> getOne();

	/**
	 * @deprecated since 2.4 in favor of {@link #getOne()}.
	 */
	default DataResponse<T> selectOne() {
		return getOne();
	}
}