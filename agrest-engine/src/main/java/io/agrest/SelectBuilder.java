package io.agrest;

import io.agrest.constraints.Constraint;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.AgBuilder;
import io.agrest.runtime.processor.select.SelectContext;

import javax.ws.rs.core.UriInfo;
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
     * Sets request {@link UriInfo} that is a source of various request parameters.
     *
     * @since 1.14
     */
    SelectBuilder<T> uri(UriInfo uriInfo);

    /**
     * Sets the Encoder of the entire response, overriding framework-provided Encoder.
     *
     * @since 3.4
     */
    SelectBuilder<T> encoder(Encoder encoder);

    /**
     * Installs request-scoped {@link EntityEncoderFilter} that allows to customize how individual JSON objects are encoded
     * within the data list. This method can be called multiple times to add more than one filter.
     *
     * @param filter a filter to apply when encoding individual entities
     * @return this builder instance
     * @see AgBuilder#entityEncoderFilter(EntityEncoderFilter)
     * @since 3.4
     */
    SelectBuilder<T> entityEncoderFilter(EntityEncoderFilter filter);

    /**
     * Installs request-scoped {@link AgEntityOverlay} that allows to customize add or change a list of properties of
     * the overlay entity available in request. This method can be called multiple times to add more than one overlay.
     * The overlay can alter the root entity, or any other entity in the model.
     *
     * @param overlay overlay descriptor
     * @param <A>     entity type for the overlay. Can be the same as "T", or may be any other model entity.
     * @return this builder instance
     * @since 3.4
     */
    <A> SelectBuilder<T> entityOverlay(AgEntityOverlay<A> overlay);

    /**
     * Defines a request-scoped attribute of the root entity of this request. This is an equivalent of calling
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
     * @since 1.7
     */
    SelectBuilder<T> parent(EntityParent<?> parent);

    /**
     * @since 1.2
     */
    SelectBuilder<T> fetchOffset(int offset);

    /**
     * @since 1.2
     */
    SelectBuilder<T> fetchLimit(int limit);

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
     * Forces the builder to make selection using explicit query parameters encapsulated in AgRequest.
     * These explicit parameters overwrite query parameters from UriInfo object.
     *
     * <pre>{@code
     *
     * 		public DataResponse<E2> getE2(@Context UriInfo uriInfo, @QueryParam CayenneExp cayenneExp) {
     * 			// Explicit query parameter
     * 			AgRequest agRequest = AgRequest.builder().cayenneExp(cayenneExp).build();
     *
     * 			return Ag.service(config).select(E2.class)
     * 							.uri(uriInfo)
     * 							.request(agRequest) // overrides parameters from uriInfo
     * 							.get();
     *        }
     *
     * }</pre>
     *
     * @param agRequest an instance of AgRequest that holds all explicit query parameters.
     * @return this builder instance.
     * @since 2.13
     */
    SelectBuilder<T> request(AgRequest agRequest);
}
