package io.agrest;

import io.agrest.access.DeleteAuthorizer;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Arrays.asList;

/**
 * @since 1.4
 */
public interface DeleteBuilder<T> {

    /**
     * Will delete an object with the specified id. Any previously set IDs are replaced with this ID. For single-value
     * ID entities, the ID argument should be a simple value (e.g. an Integer). For multi-value IDs, it should be a Map
     * of values.
     *
     * @since 5.0
     */
    default DeleteBuilder<T> byId(Object id) {
        return byIds(List.of(id));
    }

    /**
     * Will delete objects identified by the specified single-value IDs. Any previously set IDs are replaced with the
     * new collection. For single-value ID entities, array elements should be simple values (e.g. Integers). For
     * multi-value IDs, they should be represented as Maps.
     *
     * @since 5.0
     */
    default DeleteBuilder<T> byIds(Object... ids) {
        return byIds(asList(ids));
    }

    /**
     * Will delete objects identified by the specified single-value IDs. Any previously set IDs are replaced with the
     * new collection. For single-value ID entities, array elements should be simple values (e.g. Integers). For
     * multi-value IDs, they should be represented as Maps.
     *
     * @since 5.0
     */
    DeleteBuilder<T> byIds(Collection<?> ids);

    DeleteBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent);

    /**
     * @since 1.20
     */
    DeleteBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent);

    /**
     * Installs request-scoped {@link AgEntityOverlay} that allows to customize, add or redefine request entity structure
     * This method can be called multiple times to add more than one overlay.
     *
     * @param overlay overlay descriptor
     * @return this builder instance
     * @since 4.8
     */
    DeleteBuilder<T> entityOverlay(AgEntityOverlay<T> overlay);

    /**
     * @return this builder instance
     * @since 4.8
     */
    DeleteBuilder<T> authorizer(DeleteAuthorizer<T> authorizer);

    /**
     * Registers a consumer to be executed after a specified standard execution stage. The consumer can inspect and
     * modify provided {@link DeleteContext}.
     * <p>This operation is composable. For each stage all custom processors will be invoked in the order they were
     * registered.</p>
     *
     * @param afterStage  A name of the standard stage after which the inserted stage needs to be run.
     * @param customStage a callback to invoke at a specific point during  the update execution.
     * @return this builder instance.
     * @since 4.8
     */
    default <U> DeleteBuilder<T> stage(DeleteStage afterStage, Consumer<DeleteContext<U>> customStage) {
        return routingStage(afterStage, (DeleteContext<U> c) -> {
            customStage.accept(c);
            return ProcessorOutcome.CONTINUE;
        });
    }

    /**
     * Registers a consumer to be executed after the specified standard execution stage. The rest of the standard pipeline
     * following the named stage will be skipped. The consumer can inspect and modify provided {@link DeleteContext}.
     * <p>This operation is composable. For each stage all custom processors will be invoked in the order they were
     * registered.</p>
     *
     * @param afterStage          A name of the standard stage after which the inserted stage needs to be run.
     * @param customTerminalStage a consumer that will be invoked after 'afterStage', and will be the last piece of
     *                            code executed in the update pipeline.
     * @return this builder instance.
     * @since 4.8
     */
    default <U> DeleteBuilder<T> terminalStage(DeleteStage afterStage, Consumer<DeleteContext<U>> customTerminalStage) {
        return routingStage(afterStage, (DeleteContext<U> c) -> {
            customTerminalStage.accept(c);
            return ProcessorOutcome.STOP;
        });
    }

    /**
     * Registers a processor to be executed after the specified standard execution stage. The processor can inspect and
     * modify provided {@link DeleteContext}. When finished, processor can either pass control to the next stage by returning
     * {@link ProcessorOutcome#CONTINUE}, or terminate the pipeline by returning
     * {@link ProcessorOutcome#STOP}.
     * <p>This operation is composable. For each stage all custom processors will be invoked in the order they were
     * registered.</p>
     *
     * @param afterStage  A name of the standard stage after which the inserted stage needs to be run.
     * @param customStage a processor to invoke at a specific point during the update execution.
     * @return this builder instance.
     * @since 4.8
     */
    <U> DeleteBuilder<T> routingStage(DeleteStage afterStage, Processor<DeleteContext<U>> customStage);

    /**
     * Executes deletion pipeline for the request configured in this builder.
     *
     * @since 4.8
     */
    SimpleResponse sync();
}
