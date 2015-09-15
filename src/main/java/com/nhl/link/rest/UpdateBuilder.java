package com.nhl.link.rest;

import java.util.Collection;

import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.exp.Property;

import com.nhl.link.rest.annotation.listener.DataStoreUpdated;
import com.nhl.link.rest.annotation.listener.UpdateChainInitialized;
import com.nhl.link.rest.annotation.listener.UpdateRequestParsed;
import com.nhl.link.rest.annotation.listener.UpdateResponseUpdated;
import com.nhl.link.rest.annotation.listener.UpdateServerParamsApplied;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.it.fixture.cayenne.E7;
import com.nhl.link.rest.runtime.cayenne.ByKeyObjectMapperFactory;

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
	 * If not set, objects will be located by their IDs.
	 */
	UpdateBuilder<T> mapper(ObjectMapperFactory mapper);

	/**
	 * Sets a property name that should be used to map objects in update
	 * collection to backend objects. This overrides a default mapping by ID,
	 * and is equivalent to calling
	 * 'mapped(ByKeyObjectMapperFactory.byKey(propertyName))'.
	 * 
	 * @since 1.20
	 */
	UpdateBuilder<T> mapper(String propertyName);

	/**
	 * Sets a property that should be used to map objects in update collection
	 * to backend objects. This overrides a default mapping by ID, and is
	 * equivalent to calling 'mapped(ByKeyObjectMapperFactory.byKey(property))'.
	 * 
	 * @since 1.20
	 */
	UpdateBuilder<T> mapper(Property<?> property);

	/**
	 * Adds an annotated listener that will be notified of completion of
	 * individual stages during request processing. Recognized annotations are
	 * {@link UpdateChainInitialized}, {@link UpdateRequestParsed},
	 * {@link UpdateServerParamsApplied}, {@link DataStoreUpdated},
	 * {@link UpdateResponseUpdated}. Annotated method can take two forms, one
	 * that doesn't change the flow, and another one - that may:
	 * 
	 * <pre>
	 * void doSomething(UpdateContext<?> context) {
	 * }
	 * 
	 * <T> ProcessingStage<UpdateContext<T>, T> doSomethingWithTheFlow(UpdateContext<T> context,
	 * 		ProcessingStage<UpdateContext<T>, T> next) {
	 * }
	 * </pre>
	 * 
	 * @since 1.19
	 */
	UpdateBuilder<T> listener(Object listener);

	/**
	 * @deprecated since 1.19 in favor of {@link #sync(String)} and
	 *             {@link #syncAndSelect(String)}.
	 */
	@Deprecated
	DataResponse<T> process(String entityData);

	/**
	 * @since 1.19
	 */
	DataResponse<T> syncAndSelect(String entityData);

	/**
	 * @since 1.20
	 */
	DataResponse<T> syncAndSelect(EntityUpdate<T> update);

	/**
	 * @since 1.20
	 */
	DataResponse<T> syncAndSelect(Collection<EntityUpdate<T>> updates);

	/**
	 * @since 1.19
	 */
	SimpleResponse sync(String entityData);

	/**
	 * @since 1.20
	 */
	SimpleResponse sync(EntityUpdate<T> update);

	/**
	 * @since 1.20
	 */
	SimpleResponse sync(Collection<EntityUpdate<T>> updates);

}
