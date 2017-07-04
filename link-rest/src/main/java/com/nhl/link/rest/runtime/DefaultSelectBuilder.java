package com.nhl.link.rest.runtime;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.annotation.listener.DataFetched;
import com.nhl.link.rest.annotation.listener.QueryAssembled;
import com.nhl.link.rest.annotation.listener.SelectChainInitialized;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.processor2.Processor;
import com.nhl.link.rest.property.PropertyBuilder;
import com.nhl.link.rest.runtime.listener.ListenerInvocation;
import com.nhl.link.rest.runtime.listener.ListenersBuilder;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.select.SelectProcessorFactory;
import com.nhl.link.rest.runtime.processor.select.SelectStage;
import org.apache.cayenne.exp.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.lang.annotation.Annotation;
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

    private static final Map<Class<? extends Annotation>, SelectStage> ANNOTATIONS_MAP;

    static {
        ANNOTATIONS_MAP = new HashMap<>();
        ANNOTATIONS_MAP.put(SelectChainInitialized.class, SelectStage.START);
        ANNOTATIONS_MAP.put(SelectRequestParsed.class, SelectStage.PARSE_REQUEST);
        ANNOTATIONS_MAP.put(SelectServerParamsApplied.class, SelectStage.APPLY_SERVER_PARAMS);
        ANNOTATIONS_MAP.put(QueryAssembled.class, SelectStage.ASSEMBLE_QUERY);
        ANNOTATIONS_MAP.put(DataFetched.class, SelectStage.FETCH_DATA);
    }

    protected SelectContext<T> context;
    protected SelectProcessorFactory processorFactory;
    protected ListenersBuilder listenersBuilder;
    protected EnumMap<SelectStage, Processor<SelectContext<?>>> processors;

    public DefaultSelectBuilder(
            SelectContext<T> context,
            SelectProcessorFactory processorFactory,
            ListenersBuilder listenersBuilder) {
        this.context = context;
        this.processorFactory = processorFactory;
        this.listenersBuilder = listenersBuilder;
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

    private EnumMap<SelectStage, List<ListenerInvocation>> getListeners() {

        EnumMap<SelectStage, List<ListenerInvocation>> byStage = new EnumMap<>(SelectStage.class);
        listenersBuilder.getListeners().forEach((a, invocations) -> {

            SelectStage stage = ANNOTATIONS_MAP.get(a);
            if (stage != null) {
                byStage.put(stage, invocations);
            }
        });

        return byStage;
    }

    @Override
    public SelectBuilder<T> routingStage(SelectStage stage, Processor<SelectContext<?>> processor) {
        processors.compute(stage, (s, existing) -> existing != null ? existing.andThen(processor) : processor);
        return this;
    }

    @Override
    public DataResponse<T> get() {

        // 'byId' behaving as "selectOne" is really legacy behavior of 1.1...
        // should deprecate eventually
        context.setAtMostOneObject(context.isById());
        context.setListeners(listenersBuilder.getListeners());

        processorFactory
                .createProcessor(processors, getListeners())
                .execute(context);

        return context.createDataResponse();
    }

    @Override
    public DataResponse<T> getOne() {

        context.setAtMostOneObject(true);
        context.setListeners(listenersBuilder.getListeners());

        processorFactory
                .createProcessor(processors, getListeners())
                .execute(context);

        return context.createDataResponse();
    }

}
