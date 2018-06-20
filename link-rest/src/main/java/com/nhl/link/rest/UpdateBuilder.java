package com.nhl.link.rest;

import com.nhl.link.rest.annotation.listener.DataStoreUpdated;
import com.nhl.link.rest.annotation.listener.UpdateChainInitialized;
import com.nhl.link.rest.annotation.listener.UpdateRequestParsed;
import com.nhl.link.rest.annotation.listener.UpdateResponseUpdated;
import com.nhl.link.rest.annotation.listener.UpdateServerParamsApplied;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.exp.Property;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
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
     * Set an explicit id for the update. In this case only a single object is
     * allowed in the update.
     */
    UpdateBuilder<T> id(Object id);

    /**
     * Set an explicit compound id for the update. In this case only a single
     * object is allowed in the update.
     *
     * @since 1.20
     */
    UpdateBuilder<T> id(Map<String, Object> ids);

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
     * Sets up a relationship clause for all objects in this update.
     */
    UpdateBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent);

    /**
     * Sets up a relationship clause for all objects in this update.
     *
     * @since 1.20
     */
    UpdateBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, Property<T> relationshipFromParent);

    /**
     * Sets up a relationship clause for all objects in this update.
     */
    UpdateBuilder<T> toManyParent(Class<?> parentType, Object parentId,
                                  Property<? extends Collection<T>> relationshipFromParent);

    /**
     * Sets up a relationship clause for all objects in this update.
     *
     * @since 1.20
     */
    UpdateBuilder<T> toManyParent(Class<?> parentType, Map<String, Object> parentIds,
                                  Property<? extends Collection<T>> relationshipFromParent);

    /**
     * Sets request {@link UriInfo} that will be used to shape response.
     *
     * @since 1.14
     */
    UpdateBuilder<T> uri(UriInfo uriInfo);

    /**
     * Installs an optional constraint function defining how much of the request entity attributes / relationships
     * the client can see.
     *
     * @param constraint an instance of Constraint function.
     * @return this builder instance.
     * @since 2.4
     */
    UpdateBuilder<T> readConstraint(Constraint<T> constraint);

    /**
     * Installs an optional constraint function defining how much of the request entity attributes / relationships
     * the client can modify.
     *
     * @param constraint an instance of Constraint function.
     * @return this builder instance.
     */
    UpdateBuilder<T> writeConstraint(Constraint<T> constraint);

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
     * <p>
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
     * @deprecated since 2.7 use annotation-free functional form of listeners: {@link #stage(UpdateStage, Consumer)},
     * {@link #terminalStage(UpdateStage, Consumer)} and {@link #routingStage(UpdateStage, Processor)}.
     */
    UpdateBuilder<T> listener(Object listener);

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
     * initial stages of LinkRest processing, but query the data store on their own. The consumer can inspect and modify
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
