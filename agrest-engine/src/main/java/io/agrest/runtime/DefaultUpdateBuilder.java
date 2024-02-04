package io.agrest.runtime;

import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.HttpStatus;
import io.agrest.ObjectMapperFactory;
import io.agrest.RootResourceEntity;
import io.agrest.SimpleResponse;
import io.agrest.UpdateBuilder;
import io.agrest.UpdateStage;
import io.agrest.access.PathChecker;
import io.agrest.encoder.DataResponseEncoder;
import io.agrest.encoder.Encoder;
import io.agrest.id.AgObjectId;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.Processor;
import io.agrest.runtime.processor.update.BaseUpdateProcessorFactory;
import io.agrest.runtime.processor.update.UpdateContext;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
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
    public UpdateBuilder<T> clientParams(Map<String, List<String>> params) {
        this.context.mergeClientParameters(params);
        return this;
    }

    @Override
    public UpdateBuilder<T> byId(Object id) {
        context.setUnresolvedId(id);
        return this;
    }

    @Override
    public UpdateBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, AgObjectId.of(parentId), relationshipFromParent));
        return this;
    }

    @Override
    public UpdateBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, AgObjectId.ofMap(parentIds), relationshipFromParent));
        return this;
    }

    @Override
    public <A> UpdateBuilder<T> entityOverlay(AgEntityOverlay<A> overlay) {
        context.getSchema().addOverlay(overlay);
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

    @Override
    public UpdateBuilder<T> maxPathDepth(int maxPathDepth) {
        context.setMaxPathDepth(PathChecker.of(maxPathDepth));
        return this;
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
    public UpdateBuilder<T> request(AgRequest request) {
        this.context.setRequest(request);
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
        Collection<EntityUpdate<T>> updates = update != null ? Collections.singleton(update) : Collections.emptyList();
        return sync(updates);
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
        Collection<EntityUpdate<T>> updates = update != null ? Collections.singleton(update) : Collections.emptyList();
        return syncAndSelect(updates);
    }

    private SimpleResponse doSync() {
        context.setIncludingDataInResponse(false);
        processorFactory.createProcessor(processors).execute(context);

        int status = context.getResponseStatus() != null ? context.getResponseStatus() : HttpStatus.OK;
        return SimpleResponse.of(status);
    }

    private DataResponse<T> doSyncAndSelect() {
        context.setIncludingDataInResponse(true);
        processorFactory.createProcessor(processors).execute(context);
        return createDataResponse();
    }

    private DataResponse<T> createDataResponse() {

        // account for partial context stats for cases with terminal stages invoked prior
        // to those objects being created

        int status = context.getResponseStatus() != null ? context.getResponseStatus() : HttpStatus.OK;

        RootResourceEntity<T> entity = context.getEntity();
        List<T> data = entity != null ? entity.getDataWindow() : Collections.emptyList();
        int total = entity != null ? entity.getData().size() : 0;

        Encoder encoder = context.getEncoder() != null ? context.getEncoder() : DataResponseEncoder.defaultEncoder();

        return DataResponse.of(status, data).total(total).encoder(encoder).build();
    }
}
