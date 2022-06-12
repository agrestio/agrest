package io.agrest;

import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.PropertyFilter;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.protocol.ControlParams;
import io.agrest.runtime.processor.update.ByKeyObjectMapperFactory;
import io.agrest.runtime.processor.update.UpdateContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A builder for create (insert) or update operations for a single entity type.
 * Depending on how the builder was created, it will performs one of the flavors
 * of update: create, update, createOrUpdate, fullSync.
 *
 * @since 1.7
 */
public interface UpdateBuilder<T> {

    /**
     * Set an explicit id for the update. In this case only a single object is allowed in the update.
     *
     * @since 5.0
     */
    UpdateBuilder<T> byId(Object id);

    /**
     * @deprecated since 5.0 in favor of {@link #byId(Object)}
     */
    @Deprecated
    default UpdateBuilder<T> id(Object id) {
        return byId(id);
    }

    /**
     * Set an explicit multi-value id for the update. In this case only a single object is allowed in the update.
     *
     * @since 5.0
     */
    UpdateBuilder<T> byId(Map<String, Object> id);

    /**
     * @deprecated since 5.0 in favor of {@link #byId(Map)}.
     */
    @Deprecated
    default UpdateBuilder<T> id(Map<String, Object> id) {
        return byId(id);
    }

    /**
     * Sets up a relationship clause for all objects in this update.
     */
    UpdateBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent);

    /**
     * Sets up a relationship clause for all objects in this update.
     *
     * @since 1.20
     */
    UpdateBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent);

    /**
     * Sets client parameters for this request. Agrest would recognize the keys in the "params" that match the
     * "Agrest Protocol" as defined in {@link ControlParams} enum.
     *
     * @since 5.0
     */
    UpdateBuilder<T> clientParams(Map<String, List<String>> params);

    /**
     * Adds a {@link PropertyFilter} that define property read access rules for the current request and a given entity.
     * I.e. which entity attributes, relationships and ids a client is allowed to see. Can be called multiple times to add
     * multiple rules for same entity or different entities. The "entityType" parameter can match the root entity or
     * can be any other entity in the model. This method is a shortcut for
     * <code>entityOverlay(AgEntity.overlay(entityType).readablePropFilter(filter))</code>
     *
     * @return this builder instance
     * @since 4.8
     */
    default <A> UpdateBuilder<T> readablePropFilter(Class<A> entityType, PropertyFilter rules) {
        return entityOverlay(AgEntity.overlay(entityType).readablePropFilter(rules));
    }

    /**
     * Adds a {@link PropertyFilter} that define property write access rules for the current request and a given entity.
     * I.e. which entity attributes, relationships and ids a client is allowed to see. Can be called multiple times to add
     * multiple rules for same entity or different entities. The "entityType" parameter can match the root entity or
     * can be any other entity in the model. This method is a shortcut for
     * <code>entityOverlay(AgEntity.overlay(entityType).writablePropFilter(filter))</code>
     *
     * @return this builder instance
     * @since 4.10
     */
    default <A> UpdateBuilder<T> writablePropFilter(Class<A> entityType, PropertyFilter rules) {
        return entityOverlay(AgEntity.overlay(entityType).writablePropFilter(rules));
    }

    /**
     * Installs an in-memory filter for the specified entity type (not necessarily the root entity of the request).
     * The filter is applied to the response objects of the given type and will result in exclusion of objects that do
     * not match the filter. The filter is combined with any existing runtime-level filters for the same entity.
     *
     * @return this builder instance
     * @since 4.8
     */
    default <A> UpdateBuilder<T> readableFilter(Class<A> entityType, ReadFilter<A> filter) {
        return entityOverlay(AgEntity.overlay(entityType).readFilter(filter));
    }

    /**
     * @return this builder instance
     * @since 4.8
     */
    default <A> UpdateBuilder<T> createAuthorizer(Class<A> entityType, CreateAuthorizer<A> authorizer) {
        return entityOverlay(AgEntity.overlay(entityType).createAuthorizer(authorizer));
    }

    /**
     * @return this builder instance
     * @since 4.8
     */
    default <A> UpdateBuilder<T> updateAuthorizer(Class<A> entityType, UpdateAuthorizer<A> authorizer) {
        return entityOverlay(AgEntity.overlay(entityType).updateAuthorizer(authorizer));
    }

    /**
     * @return this builder instance
     * @since 4.8
     */
    default <A> UpdateBuilder<T> deleteAuthorizer(Class<A> entityType, DeleteAuthorizer<A> authorizer) {
        return entityOverlay(AgEntity.overlay(entityType).deleteAuthorizer(authorizer));
    }

    /**
     * Installs request-scoped {@link AgEntityOverlay} that allows to customize, add or redefine request entity structure,
     * e.g. change property read/write access. This method can be called multiple times to add more than one overlay.
     * The overlay can alter the root entity or any other entity in the model.
     *
     * @param overlay overlay descriptor
     * @param <A>     entity type for the overlay. Can be the same as "T", or may be any other model entity.
     * @return this builder instance
     * @since 4.8
     */
    <A> UpdateBuilder<T> entityOverlay(AgEntityOverlay<A> overlay);

    /**
     * Sets a custom mapper that locates existing objects based on request data.
     * If not set, objects will be located by their IDs.
     */
    UpdateBuilder<T> mapper(ObjectMapperFactory mapper);

    /**
     * Sets a property name that should be used to map objects in update collection to backend objects. This overrides
     * the default mapping by ID.
     *
     * @since 1.20
     */
    default UpdateBuilder<T> mapper(String propertyName) {
        return mapper(ByKeyObjectMapperFactory.byKey(propertyName));
    }

    /**
     * Registers a consumer to be executed after a specified standard execution stage. The consumer can inspect and
     * modify provided {@link UpdateContext}.
     * <p>This operation is composable. For each stage all custom processors will be invoked in the order they were
     * registered.</p>
     *
     * @param afterStage  A name of the standard stage after which the inserted stage needs to be run.
     * @param customStage a callback to invoke at a specific point during  the update execution.
     * @return this builder instance.
     * @since 2.7
     */
    default <U> UpdateBuilder<T> stage(UpdateStage afterStage, Consumer<UpdateContext<U>> customStage) {
        return routingStage(afterStage, (UpdateContext<U> c) -> {
            customStage.accept(c);
            return ProcessorOutcome.CONTINUE;
        });
    }

    /**
     * Registers a consumer to be executed after the specified standard execution stage. The rest of the standard pipeline
     * following the named stage will be skipped. This is useful for quick assembly of custom backends that reuse the
     * initial stages of Agrest processing, but query the data store on their own. The consumer can inspect and modify
     * provided {@link UpdateContext}.
     * <p>This operation is composable. For each stage all custom processors will be invoked in the order they were
     * registered.</p>
     *
     * @param afterStage          A name of the standard stage after which the inserted stage needs to be run.
     * @param customTerminalStage a consumer that will be invoked after 'afterStage', and will be the last piece of
     *                            code executed in the update pipeline.
     * @return this builder instance.
     * @since 2.7
     */
    default <U> UpdateBuilder<T> terminalStage(UpdateStage afterStage, Consumer<UpdateContext<U>> customTerminalStage) {
        return routingStage(afterStage, (UpdateContext<U> c) -> {
            customTerminalStage.accept(c);
            return ProcessorOutcome.STOP;
        });
    }

    /**
     * Registers a processor to be executed after the specified standard execution stage. The processor can inspect and
     * modify provided {@link UpdateContext}. When finished, processor can either pass control to the next stage by returning
     * {@link ProcessorOutcome#CONTINUE}, or terminate the pipeline by returning
     * {@link ProcessorOutcome#STOP}.
     * <p>This operation is composable. For each stage all custom processors will be invoked in the order they were
     * registered.</p>
     *
     * @param afterStage  A name of the standard stage after which the inserted stage needs to be run.
     * @param customStage a processor to invoke at a specific point during the update execution.
     * @return this builder instance.
     * @since 2.7
     */
    <U> UpdateBuilder<T> routingStage(UpdateStage afterStage, Processor<UpdateContext<U>> customStage);

    /**
     * Installs explicit query parameters encapsulated in AgRequest.
     * These explicit parameters overwrite query parameters from UriInfo object.
     *
     * <pre>{@code
     *
     * 		public DataResponse<E> getE(@Context UriInfo uriInfo, @QueryParam String exp) {
     * 			// Explicit query parameter
     * 			AgRequest agRequest = AgRequest.builder().exp(exp).build();
     *
     * 			return Ag.service(config).select(E.class)
     * 							.uri(uriInfo)
     * 							.request(agRequest) // overwrite parameters from uriInfo
     * 							.get();
     *        }
     *
     * }</pre>
     *
     * @param agRequest an instance of AgRequest that holds all explicit query parameters.
     * @return this builder instance.
     * @since 2.13
     */
    UpdateBuilder<T> request(AgRequest agRequest);

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
