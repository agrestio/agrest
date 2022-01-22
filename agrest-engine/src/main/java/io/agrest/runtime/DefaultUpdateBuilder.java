package io.agrest.runtime;

import io.agrest.AgException;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapperFactory;
import io.agrest.SimpleResponse;
import io.agrest.UpdateBuilder;
import io.agrest.UpdateStage;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.Processor;
import io.agrest.runtime.processor.update.BaseUpdateProcessorFactory;
import io.agrest.runtime.processor.update.ByKeyObjectMapperFactory;
import io.agrest.runtime.processor.update.UpdateContext;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * @since 1.7
 */
public class DefaultUpdateBuilder<T> implements UpdateBuilder<T> {

    private final UpdateContext<T> context;
    protected final BaseUpdateProcessorFactory processorFactory;
    protected final EnumMap<UpdateStage, Processor<UpdateContext<?>>> processors;

    public DefaultUpdateBuilder(
            UpdateContext<T> context,
            BaseUpdateProcessorFactory processorFactory) {

        this.context = context;
        this.processorFactory = processorFactory;
        this.processors = new EnumMap<>(UpdateStage.class);
    }

    @Override
    public UpdateBuilder<T> uri(UriInfo uriInfo) {
        context.setUriInfo(uriInfo);
        return this;
    }

    @Override
    public UpdateBuilder<T> byId(Object id) {
        context.setId(id);
        return this;
    }

    @Override
    public UpdateBuilder<T> byId(Map<String, Object> id) {

        for (Map.Entry<String, Object> e : id.entrySet()) {
            if (e.getValue() == null) {
                throw AgException.notFound("Part of compound ID is null: %s", e.getKey());
            }
        }

        context.setCompoundId(id);
        return this;
    }

    @Override
    public UpdateBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent));
        return this;
    }

    @Override
    public UpdateBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentIds, relationshipFromParent));
        return this;
    }

    @Override
    public <A> UpdateBuilder<T> entityOverlay(AgEntityOverlay<A> overlay) {
        context.addEntityOverlay(overlay);
        return this;
    }

    /**
     * @since 1.4
     */
    @Override
    public UpdateBuilder<T> mapper(ObjectMapperFactory mapper) {
        context.setMapper(mapper);
        return this;
    }

    /**
     * @since 1.20
     */
    @Override
    public UpdateBuilder<T> mapper(String propertyName) {
        return mapper(ByKeyObjectMapperFactory.byKey(propertyName));
    }

    /**
     * @since 2.7
     */
    @Override
    public <U> UpdateBuilder<T> routingStage(UpdateStage afterStage, Processor<UpdateContext<U>> customStage) {
        return routingStage_NoGenerics(afterStage, customStage);
    }

    private UpdateBuilder<T> routingStage_NoGenerics(UpdateStage afterStage, Processor customStage) {
        processors.compute(afterStage, (s, existing) -> existing != null ? existing.andThen(customStage) : customStage);
        return this;
    }

    /**
     * @since 2.13
     */
    @Override
    public UpdateBuilder<T> request(AgRequest agRequest) {
        this.context.setRequest(agRequest);
        return this;
    }

    /**
     * @since 1.19
     */
    @Override
    public SimpleResponse sync(String entityData) {
        context.setEntityData(entityData);
        return doSync();
    }

    /**
     * @since 1.20
     */
    @Override
    public SimpleResponse sync(EntityUpdate<T> update) {
        return sync(Collections.singleton(update));
    }

    /**
     * @since 1.20
     */
    @Override
    public SimpleResponse sync(Collection<EntityUpdate<T>> updates) {
        context.setUpdates(updates);
        return doSync();
    }

    /**
     * @since 1.19
     */
    @Override
    public DataResponse<T> syncAndSelect(String entityData) {
        context.setEntityData(entityData);
        return doSyncAndSelect();
    }

    /**
     * @since 1.20
     */
    @Override
    public DataResponse<T> syncAndSelect(Collection<EntityUpdate<T>> updates) {
        context.setUpdates(updates);
        return doSyncAndSelect();
    }

    /**
     * @since 1.20
     */
    @Override
    public DataResponse<T> syncAndSelect(EntityUpdate<T> update) {
        return syncAndSelect(Collections.singleton(update));
    }

    private SimpleResponse doSync() {
        context.setIncludingDataInResponse(false);
        processorFactory.createProcessor(processors).execute(context);
        return context.createSimpleResponse();
    }

    private DataResponse<T> doSyncAndSelect() {
        context.setIncludingDataInResponse(true);
        processorFactory.createProcessor(processors).execute(context);
        return context.createDataResponse();
    }
}
