package com.nhl.link.rest.runtime;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SelectStage;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.property.PropertyBuilder;
import com.nhl.link.rest.runtime.listener.IListenerService;
import com.nhl.link.rest.runtime.listener.SelectListenersBuilder;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.select.SelectProcessorFactory;
import org.apache.cayenne.exp.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 1.16
 */
public class DefaultSelectBuilder<T> implements SelectBuilder<T> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultSelectBuilder.class);

    protected SelectContext<T> context;
    protected SelectProcessorFactory processorFactory;
    protected SelectListenersBuilder listenersBuilder;
    protected EnumMap<SelectStage, Processor<SelectContext<?>>> processors;

    public DefaultSelectBuilder(
            SelectContext<T> context,
            SelectProcessorFactory processorFactory,
            IListenerService listenerService) {
        this.context = context;
        this.processorFactory = processorFactory;
        this.listenersBuilder = new SelectListenersBuilder(this, listenerService, context);
        this.processors = new EnumMap<>(SelectStage.class);
    }

    public SelectContext<T> getContext() {
        return context;
    }

    @Override
    public SelectBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent.getName()));
        return this;
    }

    @Override
    public SelectBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds,
                                   Property<T> relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentIds, relationshipFromParent.getName()));
        return this;
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

    @Override
    public SelectBuilder<T> toManyParent(Class<?> parentType, Object parentId,
                                         Property<? extends Collection<T>> relationshipFromParent) {
        return parent(parentType, parentId, relationshipFromParent.getName());
    }

    @Override
    public SelectBuilder<T> toManyParent(Class<?> parentType, Map<String, Object> parentIds,
                                         Property<? extends Collection<T>> relationshipFromParent) {
        return parent(parentType, parentIds, relationshipFromParent.getName());
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

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> queryParams(Map<String, List<String>> parameters) {
        this.context.setQueryParams(parameters);
        return this;
    }

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> queryParam(String name, List<String> value) {
        getOrCreateQueryParams().put(name, value);
        return this;
    }

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> queryParam(String name, String value) {
        getOrCreateQueryParams().put(name, Arrays.asList(value));
        return this;
    }

    @Override
    public SelectBuilder<T> dataEncoder(Encoder encoder) {
        this.context.setEncoder(encoder);
        return this;
    }

    @Override
    public SelectBuilder<T> autocompleteOn(Property<?> autocompleteProperty) {
        context.setAutocompleteProperty(autocompleteProperty != null ? autocompleteProperty.getName() : null);
        return this;
    }

    @Override
    public SelectBuilder<T> property(String name) {
        return property(name, PropertyBuilder.property());
    }

    @Override
    public SelectBuilder<T> property(String name, EntityProperty clientProperty) {
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
    public SelectBuilder<T> byId(Object id) {
        // TODO: return a special builder that will preserve 'byId' strategy on
        // select

        if (id == null) {
            throw new LinkRestException(Status.NOT_FOUND, "Null 'id'");
        }

        context.setId(id);
        return this;
    }

    @Override
    public SelectBuilder<T> byId(Map<String, Object> ids) {

        for (Object id : ids.entrySet()) {
            if (id == null) {
                throw new LinkRestException(Status.NOT_FOUND, "Part of compound ID is null");
            }
        }

        context.setCompoundId(ids);
        return this;
    }

    /**
     * @since 1.19
     */
    @Override
    public SelectBuilder<T> listener(Object listener) {
        listenersBuilder.addListener(listener);
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

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> mapBy(String mapBy) {
        getOrCreateQueryParams().put(MAP_BY, Arrays.asList(mapBy));
        return this;
    }

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> cayenneExp(String cayenneExp) {
        getOrCreateQueryParams().put(CAYENNE_EXP, Arrays.asList(cayenneExp));
        return this;
    }

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> start(long start) {
        return null;
    }

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> limit(long limit) {
        return null;
    }

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> exclude(List<String> exclude) {
        getOrCreateQueryParams().put(EXCLUDE, exclude);
        return this;
    }

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> include(List<String> include) {
        getOrCreateQueryParams().put(INCLUDE, include);
        return this;
    }

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> sort(List<String> sort) {
        getOrCreateQueryParams().put(SORT, sort);
        return this;
    }

    /**
     * @since 2.13
     */
    @Override
    public SelectBuilder<T> dir(String dir) {
        getOrCreateQueryParams().put(DIR, Arrays.asList(dir));
        return this;
    }

    private Map<String, List<String>> getOrCreateQueryParams() {
        if (context.getQueryParams() == null) {
            context.setQueryParams(new HashMap<>());
        }

        return context.getQueryParams();
    }
}
