package io.agrest.runtime;

import io.agrest.DeleteBuilder;
import io.agrest.DeleteStage;
import io.agrest.HttpStatus;
import io.agrest.SimpleResponse;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.id.AgObjectId;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.Processor;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * @since 1.4
 */
public class DefaultDeleteBuilder<T> implements DeleteBuilder<T> {

    protected final DeleteContext<T> context;
    protected final DeleteProcessorFactory processorFactory;
    protected final EnumMap<DeleteStage, Processor<DeleteContext<?>>> processors;

    public DefaultDeleteBuilder(DeleteContext<T> context, DeleteProcessorFactory processorFactory) {
        this.context = context;
        this.processorFactory = processorFactory;
        this.processors = new EnumMap<>(DeleteStage.class);
    }

    @Override
    public DeleteBuilder<T> byIds(Collection<?> ids) {
        context.setUnresolvedIds(ids);
        return this;
    }

    @Override
    public DeleteBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, AgObjectId.of(parentId), relationshipFromParent));
        return this;
    }

    @Override
    public DeleteBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, AgObjectId.ofMap(parentIds), relationshipFromParent));
        return this;
    }

    @Override
    public DeleteBuilder<T> entityOverlay(AgEntityOverlay<T> overlay) {
        context.getSchema().addOverlay(overlay);
        return this;
    }

    @Override
    public DeleteBuilder<T> authorizer(DeleteAuthorizer<T> authorizer) {
        return entityOverlay(AgEntity.overlay(context.getType()).deleteAuthorizer(authorizer));
    }

    @Override
    public <U> DeleteBuilder<T> routingStage(DeleteStage afterStage, Processor<DeleteContext<U>> customStage) {
        return routingStage_NoGenerics(afterStage, customStage);
    }

    private DeleteBuilder<T> routingStage_NoGenerics(DeleteStage afterStage, Processor customStage) {
        processors.compute(afterStage, (s, existing) -> existing != null ? existing.andThen(customStage) : customStage);
        return this;
    }

    @Override
    public SimpleResponse sync() {
        processorFactory.createProcessor(processors).execute(context);
        int status = context.getResponseStatus() != null ? context.getResponseStatus() : HttpStatus.OK;
        return SimpleResponse.of(status);
    }
}
