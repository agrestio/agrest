package io.agrest.runtime;

import io.agrest.AgException;
import io.agrest.DataResponse;
import io.agrest.EntityParent;
import io.agrest.EntityProperty;
import io.agrest.AgRequest;
import io.agrest.SelectBuilder;
import io.agrest.SelectStage;
import io.agrest.SizeConstraints;
import io.agrest.constraints.Constraint;
import io.agrest.encoder.Encoder;
import io.agrest.processor.Processor;
import io.agrest.property.PropertyBuilder;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.16
 */
public class DefaultSelectBuilder<T, E> implements SelectBuilder<T, E> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSelectBuilder.class);

    protected SelectContext<T, E> context;
    protected SelectProcessorFactory processorFactory;
    protected EnumMap<SelectStage, Processor<SelectContext<?, ?>>> processors;

    public DefaultSelectBuilder(
            SelectContext<T, E> context,
            SelectProcessorFactory processorFactory) {
        this.context = context;
        this.processorFactory = processorFactory;
        this.processors = new EnumMap<>(SelectStage.class);
    }

    public SelectContext<T, E> getContext() {
        return context;
    }

    @Override
    public SelectBuilder<T, E> parent(Class<?> parentType, Object parentId, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent));
        return this;
    }

    @Override
    public SelectBuilder<T, E> parent(Class<?> parentType, Map<String, Object> parentIds, String relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentIds, relationshipFromParent));
        return this;
    }

    @Override
    public SelectBuilder<T, E> parent(EntityParent<?> parent) {
        context.setParent(parent);
        return this;
    }

    @Override
    public SelectBuilder<T, E> toManyParent(Class<?> parentType, Object parentId,
                                         String relationshipFromParent) {
        return parent(parentType, parentId, relationshipFromParent);
    }

    @Override
    public SelectBuilder<T, E> toManyParent(Class<?> parentType, Map<String, Object> parentIds,
                                         String relationshipFromParent) {
        return parent(parentType, parentIds, relationshipFromParent);
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
    public SelectBuilder<T, E> constraint(Constraint<T, E> constraint) {
        context.setConstraint(constraint);
        return this;
    }

    @Override
    public SelectBuilder<T, E> fetchLimit(int limit) {
        getOrCreateSizeConstraints().fetchLimit(limit);
        return this;
    }

    @Override
    public SelectBuilder<T, E> fetchOffset(int offset) {
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
    public SelectBuilder<T, E> uri(UriInfo uriInfo) {
        this.context.setUriInfo(uriInfo);
        return this;
    }

    @Override
    public SelectBuilder<T, E> dataEncoder(Encoder encoder) {
        this.context.setEncoder(encoder);
        return this;
    }

    @Override
    public SelectBuilder<T, E> property(String name) {
        return property(name, PropertyBuilder.property());
    }

    @Override
    public SelectBuilder<T, E> property(String name, EntityProperty clientProperty) {
        if (context.getExtraProperties() == null) {
            context.setExtraProperties(new HashMap<>());
        }

        EntityProperty oldProperty = context.getExtraProperties().put(name, clientProperty);
        if (oldProperty != null) {
            logger.info("Overriding existing custom property '" + name + "', ignoring...");
        }

        return this;
    }

    @Override
    public SelectBuilder<T, E> byId(Object id) {
        // TODO: return a special builder that will preserve 'byId' strategy on
        // select

        if (id == null) {
            throw new AgException(Status.NOT_FOUND, "Null 'id'");
        }

        context.setId(id);
        return this;
    }

    @Override
    public SelectBuilder<T, E> byId(Map<String, Object> ids) {

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
    public <U> SelectBuilder<T, E> routingStage(SelectStage afterStage, Processor<SelectContext<U, E>> customStage) {
        return routingStage_NoGenerics(afterStage, customStage);
    }

    private SelectBuilder<T, E> routingStage_NoGenerics(SelectStage afterStage, Processor customStage) {
        processors.compute(afterStage, (s, existing) -> existing != null ? existing.andThen(customStage) : customStage);
        return this;
    }

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T, E> request(AgRequest agRequest) {
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
