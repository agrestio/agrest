package io.agrest.runtime;

import io.agrest.*;
import io.agrest.constraints.Constraint;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.Processor;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @since 1.16
 */
public class DefaultSelectBuilder<T> implements SelectBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSelectBuilder.class);

    protected SelectContext<T> context;
    protected SelectProcessorFactory processorFactory;
    protected EnumMap<SelectStage, Processor<SelectContext<?>>> processors;

    public DefaultSelectBuilder(
            SelectContext<T> context,
            SelectProcessorFactory processorFactory) {
        this.context = context;
        this.processorFactory = processorFactory;
        this.processors = new EnumMap<>(SelectStage.class);
    }

    public SelectContext<T> getContext() {
        return context;
    }

    @Override
    public SelectBuilder<T> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent));
        return this;
    }

    @Override
    public SelectBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentIds, relationshipFromParent));
        return this;
    }

    @Override
    public SelectBuilder<T> parent(EntityParent<?> parent) {
        context.setParent(parent);
        return this;
    }

    /**
     * Installs an optional constraint function defining how much of the request entity attributes / relationships
     * the client can see.
     *
     * @param constraint an instance of Constraint function.
     * @return this builder instance.
     * @since 2.4
     */
    @Override
    public SelectBuilder<T> constraint(Constraint<T> constraint) {
        context.setConstraint(constraint);
        return this;
    }

    @Override
    public SelectBuilder<T> fetchLimit(int limit) {
        getOrCreateSizeConstraints().fetchLimit(limit);
        return this;
    }

    @Override
    public SelectBuilder<T> fetchOffset(int offset) {
        getOrCreateSizeConstraints().fetchOffset(offset);
        return this;
    }

    private SizeConstraints getOrCreateSizeConstraints() {
        if (context.getSizeConstraints() == null) {
            context.setSizeConstraints(new SizeConstraints());
        }

        return context.getSizeConstraints();
    }

    @Override
    public SelectBuilder<T> uri(UriInfo uriInfo) {
        this.context.setUriInfo(uriInfo);
        return this;
    }

    @Override
    public SelectBuilder<T> encoder(Encoder encoder) {
        this.context.setEncoder(encoder);
        return this;
    }

    /**
     * @since 3.4
     */
    @Override
    public SelectBuilder<T> entityEncoderFilter(EntityEncoderFilter filter) {
        if (context.getEntityEncoderFilters() == null) {
            context.setEntityEncoderFilters(new ArrayList<>());
        }

        context.getEntityEncoderFilters().add(filter);
        return this;
    }

    @Override
    public <A> SelectBuilder<T> entityOverlay(AgEntityOverlay<A> overlay) {
        context.addEntityOverlay(overlay);
        return this;
    }

    @Override
    public <V> SelectBuilder<T> entityAttribute(String name, Class<V> valueType, Function<T, V> reader) {
        return entityOverlay(AgEntity.overlay(getContext().getType()).redefineAttribute(name, valueType, reader));
    }

    @Override
    public SelectBuilder<T> byId(Object id) {
        // TODO: return a special builder that will preserve 'byId' strategy on
        // select

        if (id == null) {
            throw new AgException(Status.NOT_FOUND, "Null 'id'");
        }

        context.setId(id);
        return this;
    }

    @Override
    public SelectBuilder<T> byId(Map<String, Object> ids) {

        for (Object id : ids.entrySet()) {
            if (id == null) {
                throw new AgException(Status.NOT_FOUND, "Part of compound ID is null");
            }
        }

        context.setCompoundId(ids);
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
    public SelectBuilder<T> request(AgRequest agRequest) {
        this.context.setRequest(agRequest);
        return this;
    }

    @Override
    public DataResponse<T> get() {

        // 'byId' behaving as "selectOne" is really legacy behavior of 1.1...
        // should deprecate eventually
        context.setAtMostOneObject(context.isById());
        processorFactory.createProcessor(processors).execute(context);
        return context.createDataResponse();
    }

    @Override
    public DataResponse<T> getOne() {
        context.setAtMostOneObject(true);
        processorFactory.createProcessor(processors).execute(context);
        return context.createDataResponse();
    }
}
