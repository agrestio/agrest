package io.agrest;

import io.agrest.access.PropertyFilter;
import io.agrest.access.ReadFilter;
import io.agrest.encoder.Encoder;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.protocol.ControlParams;
import io.agrest.runtime.processor.select.SelectContext;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An object that allows to customize/extend Agrest request processing. SelectBuilder instance is created by Agrest
 * and then configured by the user server-side code to achieve the desired behavior. Finally {@link #get()} is called
 * to get the results.
 */
public interface SelectBuilder<T> {

    /**
     * Sets client parameters for this request. Agrest would recognize the keys in the "params" that match the
     * "Agrest Protocol" as defined in {@link ControlParams} enum.
     *
     * @since 5.0
     */
    SelectBuilder<T> clientParams(Map<String, List<String>> params);

    /**
     * Sets the Encoder of the entire response, overriding framework-provided Encoder.
     *
     * @since 3.4
     */
    SelectBuilder<T> encoder(Encoder encoder);

    /**
     * Installs request-scoped {@link AgEntityOverlay} that allows to customize, add or redefine client-accessible
     * properties of the overlay entity available in request. This method can be called multiple times to add more
     * than one overlay. The overlay can alter the root entity or any other entity in the model.
     *
     * @param overlay overlay descriptor
     * @param <A>     entity type for the overlay. Can be the same as "T", or may be any other model entity.
     * @return this builder instance
     * @since 3.4
     */
    <A> SelectBuilder<T> entityOverlay(AgEntityOverlay<A> overlay);

    /**
     * Defines a custom request-scoped attribute of the root entity of this request. This is an equivalent of calling
     * {@link #entityOverlay(AgEntityOverlay)} with the overlay containing a single custom attribute.
     *
     * @since 3.7
     */
    <V> SelectBuilder<T> entityAttribute(String name, Class<V> valueType, Function<T, V> reader);

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
     * Appends a {@link PropertyFilter} that defines property access rules for the current request and a given entity. I.e.
     * which entity attributes, relationships and ids a client is allowed to see. Can be called multiple times to add
     * multiple rules for same entity or different entities. The "entityType" parameter can match the root entity or
     * can be any other entity in the model.
     *
     * <p>This method is a shortcut for <code>entityOverlay(AgEntity.overlay(entityType).readablePropFilter(filter))</code></p>
     *
     * @return this builder instance
     * @since 4.8
     */
    default <A> SelectBuilder<T> propFilter(Class<A> entityType, PropertyFilter filter) {
        return entityOverlay(AgEntity.overlay(entityType).readablePropFilter(filter));
    }

    /**
     * Installs an in-memory filter for the specified entity type (not necessarily the root entity of the request).
     * Response will exclude objects that do not match the filter. This filter is combined with any existing
     * runtime-level filters for the same entity.
     *
     * @return this builder instance
     * @since 4.8
     */
    default <A> SelectBuilder<T> filter(Class<A> entityType, ReadFilter<A> filter) {
        return entityOverlay(AgEntity.overlay(entityType).readFilter(filter));
    }

    /**
     * @since 1.4
     */
    SelectBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent);

    /**
     * @since 1.20
     */
    SelectBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent);

    /**
     * Defines an offset for the result. Used to provide server-side pagination.
     *
     * @since 5.0
     */
    SelectBuilder<T> start(int start);

    /**
     * @since 1.2
     */
    SelectBuilder<T> limit(int limit);

    /**
     * @deprecated since 5.0 in favor of {@link #start(int)} to match the name of the Ag protocol parameter.
     */
    @Deprecated
    default SelectBuilder<T> fetchOffset(int offset) {
        return start(offset);
    }

    /**
     * @deprecated since 5.0 in favor of {@link #limit(int)} to match the name of the Ag protocol parameter.
     */
    @Deprecated
    default SelectBuilder<T> fetchLimit(int limit) {
        return limit(limit);
    }

    /**
     * Registers a consumer to be executed after a specified standard execution stage. The consumer can inspect and
     * modify provided {@link SelectContext}.
     * <p>This operation is composable. For each stage all custom processors will be invoked in the order they were
     * registered.</p>
     *
     * @param afterStage  A name of the standard stage after which the inserted stage needs to be run.
     * @param customStage a callback to invoke at a specific point during select execution.
     * @return this builder instance.
     * @since 2.7
     */
    default <U> SelectBuilder<T> stage(SelectStage afterStage, Consumer<SelectContext<U>> customStage) {
        return routingStage(afterStage, (SelectContext<U> c) -> {
            customStage.accept(c);
            return ProcessorOutcome.CONTINUE;
        });
    }

    /**
     * Registers a consumer to be executed after the specified standard execution stage. The rest of the standard pipeline
     * following the named stage will be skipped. This is useful for quick assembly of custom backends that reuse the
     * initial stages of Agrest processing, but query the data store on their own. The consumer can inspect and modify
     * provided {@link SelectContext}.
     * <p>This operation is composable. For each stage all custom processors will be invoked in the order they were
     * registered.</p>
     *
     * @param afterStage          A name of the standard stage after which the inserted stage needs to be run.
     * @param customTerminalStage a consumer that will be invoked after 'afterStage', and will be the last piece of
     *                            code executed in the select pipeline.
     * @return this builder instance.
     * @since 2.7
     */
    default <U> SelectBuilder<T> terminalStage(SelectStage afterStage, Consumer<SelectContext<U>> customTerminalStage) {
        return routingStage(afterStage, (SelectContext<U> c) -> {
            customTerminalStage.accept(c);
            return ProcessorOutcome.STOP;
        });
    }

    /**
     * Registers a processor to be executed after the specified standard execution stage. The processor can inspect and
     * modify provided {@link SelectContext}. When finished, processor can either pass control to the next stage by returning
     * {@link ProcessorOutcome#CONTINUE}, or terminate the pipeline by returning
     * {@link ProcessorOutcome#STOP}.
     * <p>This operation is composable. For each stage all custom processors will be invoked in the order they were
     * registered.</p>
     *
     * @param afterStage  A name of the standard stage after which the inserted stage needs to be run.
     * @param customStage a processor to invoke at a specific point during select execution.
     * @return this builder instance.
     * @since 2.7
     */
    <U> SelectBuilder<T> routingStage(SelectStage afterStage, Processor<SelectContext<U>> customStage);

    /**
     * Runs the query corresponding to the state of this builder, returning
     * a response that can be serialized by the framework.
     *
     * @since 2.4
     */
    DataResponse<T> get();

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
     * A utility method to quickly return an empty success response, without running a query. The implementation may
     * still process request parameters and report errors in request.
     *
     * @since 4.4
     */
    DataResponse<T> getEmpty();

    /**
     * Forces the builder to make selection using explicit query parameters encapsulated in AgRequest.
     * These explicit parameters overwrite query parameters from UriInfo object.
     *
     * <pre>{@code
     *
     * 		public DataResponse<E> getE(@Context UriInfo uriInfo, @QueryParam String exp) {
     * 			// Explicit query parameter
     * 			AgRequest agRequest = AgRequest.builder().exp(exp).build();
     *
     * 			return Ag.service(config).select(E.class)
     * 							.uri(uriInfo) // this may not even be needed
     * 							.request(agRequest) // overrides parameters from uriInfo
     * 							.get();
     *        }
     *
     * }</pre>
     *
     * @param request an instance of AgRequest that holds all explicit query parameters.
     * @return this builder instance.
     * @since 2.13
     */
    SelectBuilder<T> request(AgRequest request);
}
