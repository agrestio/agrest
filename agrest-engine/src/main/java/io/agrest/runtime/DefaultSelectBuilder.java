package io.agrest.runtime;

import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.HttpStatus;
import io.agrest.RootResourceEntity;
import io.agrest.SelectBuilder;
import io.agrest.SelectStage;
import io.agrest.SizeConstraints;
import io.agrest.access.PathChecker;
import io.agrest.encoder.DataResponseEncoder;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.GenericEncoder;
import io.agrest.encoder.ListEncoder;
import io.agrest.id.AgObjectId;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgSchema;
import io.agrest.processor.Processor;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.processor.select.SelectProcessorFactory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @since 1.16
 */
public class DefaultSelectBuilder<T> implements SelectBuilder<T> {

    protected final SelectContext<T> context;
    protected final SelectProcessorFactory processorFactory;
    protected final EnumMap<SelectStage, Processor<SelectContext<?>>> processors;

    public DefaultSelectBuilder(SelectContext<T> context, SelectProcessorFactory processorFactory) {
        this.context = context;
        this.processorFactory = processorFactory;
        this.processors = new EnumMap<>(SelectStage.class);
    }

    public SelectContext<T> getContext() {
        return context;
    }

    @Override
    public SelectBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
        AgEntity<?> parentEntity = context.service(AgSchema.class).getEntity(parentType);
        context.setParent(new EntityParent<>(parentEntity, AgObjectId.of(parentId), relationshipFromParent));
        return this;
    }

    @Override
    public SelectBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent) {
        AgEntity<?> parentEntity = context.service(AgSchema.class).getEntity(parentType);
        context.setParent(new EntityParent<>(parentEntity, AgObjectId.ofMap(parentIds), relationshipFromParent));
        return this;
    }

    @Override
    public SelectBuilder<T> limit(int limit) {
        getOrCreateSizeConstraints().fetchLimit(limit);
        return this;
    }

    @Override
    public SelectBuilder<T> start(int offset) {
        getOrCreateSizeConstraints().fetchOffset(offset);
        return this;
    }

    @Override
    public SelectBuilder<T> maxPathDepth(int maxPathDepth) {
        context.setMaxPathDepth(PathChecker.of(maxPathDepth));
        return this;
    }

    private SizeConstraints getOrCreateSizeConstraints() {
        if (context.getSizeConstraints() == null) {
            context.setSizeConstraints(new SizeConstraints());
        }

        return context.getSizeConstraints();
    }

    @Override
    public SelectBuilder<T> clientParams(Map<String, List<String>> params) {
        this.context.mergeClientParameters(params);
        return this;
    }

    @Override
    public SelectBuilder<T> encoder(Encoder encoder) {
        this.context.setEncoder(encoder);
        return this;
    }

    @Override
    public <A> SelectBuilder<T> entityOverlay(AgEntityOverlay<A> overlay) {
        context.addEntityOverlay(overlay);
        return this;
    }

    @Override
    public <V> SelectBuilder<T> entityAttribute(String name, Class<V> valueType, Function<T, V> reader) {
        return entityOverlay(AgEntity.overlay(getContext().getType()).attribute(name, valueType, reader));
    }

    @Override
    public SelectBuilder<T> byId(Object id) {
        // TODO: return a special builder that will preserve 'byId' strategy on select
        context.setId(AgObjectId.of(id));
        return this;
    }

    @Override
    public SelectBuilder<T> byId(Map<String, Object> id) {
        context.setId(AgObjectId.ofMap(id));
        return this;
    }

    /**
     * @since 2.7
     */
    @Override
    public <U> SelectBuilder<T> routingStage(SelectStage afterStage, Processor<SelectContext<U>> customStage) {
        return routingStage_NoGenerics(afterStage, customStage);
    }

    private SelectBuilder<T> routingStage_NoGenerics(SelectStage afterStage, Processor customStage) {
        processors.compute(afterStage, (s, existing) -> existing != null ? existing.andThen(customStage) : customStage);
        return this;
    }

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> request(AgRequest request) {
        this.context.setRequest(request);
        return this;
    }

    @Override
    public DataResponse<T> get() {

        // 'byId' behaving as "selectOne" is really legacy behavior of 1.1...
        // should deprecate eventually
        context.setAtMostOneObject(context.isById());
        processorFactory.createProcessor(processors).execute(context);
        return createDataResponse();
    }

    @Override
    public DataResponse<T> getOne() {
        context.setAtMostOneObject(true);
        processorFactory.createProcessor(processors).execute(context);
        return createDataResponse();
    }

    @Override
    public DataResponse<T> getEmpty() {
        return terminalStage(SelectStage.APPLY_SERVER_PARAMS, this::processEmpty).get();
    }

    private DataResponse<T> createDataResponse() {

        // account for partial context stats for cases with terminal stages invoked prior
        // to those objects being created

        int status = context.getResponseStatus() != null ? context.getResponseStatus() : HttpStatus.OK;

        RootResourceEntity<T> entity = context.getEntity();
        List<T> data = entity != null ? entity.getDataWindow() : Collections.emptyList();
        int total = entity != null ? entity.getData().size() : 0;

        Encoder encoder = context.getEncoder() != null ? context.getEncoder() : defaultEncoder();

        return DataResponse.of(status, data).total(total).encoder(encoder).build();
    }

    private void processEmpty(SelectContext<T> context) {
        context.getEntity().setData(Collections.emptyList());
        processorFactory.getStageProcessor(SelectStage.ENCODE).execute(context);
    }

    private Encoder defaultEncoder() {
        return new DataResponseEncoder(
                "data",
                new ListEncoder(GenericEncoder.encoder()),
                "total",
                GenericEncoder.encoder());
    }
}
