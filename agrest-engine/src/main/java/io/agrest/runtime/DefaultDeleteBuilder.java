package io.agrest.runtime;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.DeleteBuilder;
import io.agrest.DeleteStage;
import io.agrest.EntityParent;
import io.agrest.SimpleResponse;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.Processor;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.DeleteProcessorFactory;

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
    public DeleteBuilder<T> id(Object id) {
        context.addId(id);
        return this;
    }

    @Override
    public DeleteBuilder<T> id(Map<String, Object> ids) {

        ids.forEach((name, value) -> {
            if (value == null) {
                throw AgException.notFound("Part of compound ID is null: %s", name);
            }
        });

        context.addCompoundId(ids);
        return this;
    }

    @Override
    public DeleteBuilder<T> id(AgObjectId id) {
        context.addId(id);
        return this;
    }

    @Override
    public DeleteBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent));
        return this;
    }

    @Override
    public DeleteBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentIds, relationshipFromParent));
        return this;
    }

    @Override
    public DeleteBuilder<T> entityOverlay(AgEntityOverlay<T> overlay) {
        context.addEntityOverlay(overlay);
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
    public SimpleResponse delete() {
        processorFactory.createProcessor(processors).execute(context);
        return context.createSimpleResponse();
    }
}
