package com.nhl.link.rest.runtime;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ObjectMapperFactory;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.UpdateStage;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.cayenne.ByKeyObjectMapperFactory;
import com.nhl.link.rest.runtime.listener.IListenerService;
import com.nhl.link.rest.runtime.listener.UpdateListenersBuilder;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;
import com.nhl.link.rest.runtime.processor.update.UpdateProcessorFactory;
import org.apache.cayenne.exp.Property;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 1.7
 */
public class DefaultUpdateBuilder<T> implements UpdateBuilder<T> {

    private UpdateContext<T> context;
    private UpdateListenersBuilder listenersBuilder;
    protected UpdateProcessorFactory processorFactory;
    protected EnumMap<UpdateStage, Processor<UpdateContext<?>>> processors;

    public DefaultUpdateBuilder(
            UpdateContext<T> context,
            UpdateProcessorFactory processorFactory,
            IListenerService listenerService) {

        this.context = context;
        this.listenersBuilder = new UpdateListenersBuilder(this, listenerService, context);
        this.processorFactory = processorFactory;
        this.processors = new EnumMap<>(UpdateStage.class);
    }

    @Override
    public UpdateBuilder<T> uri(UriInfo uriInfo) {
        context.setUriInfo(uriInfo);
        return this;
    }

    @Override
    public UpdateBuilder<T> id(Object id) {
        context.setId(id);
        return this;
    }

    @Override
    public UpdateBuilder<T> id(Map<String, Object> ids) {

        for (Object id : ids.entrySet()) {
            if (id == null) {
                throw new LinkRestException(Response.Status.NOT_FOUND, "Part of compound ID is null");
            }
        }

        context.setCompoundId(ids);
        return this;
    }

    @Override
    public UpdateBuilder<T> parent(Class<?> parentType, Object parentId, Property<T> relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentId, relationshipFromParent.getName()));
        return this;
    }

    @Override
    public UpdateBuilder<T> parent(Class<?> parentType, Map<String, Object> parentIds,
                                   Property<T> relationshipFromParent) {
        context.setParent(new EntityParent<>(parentType, parentIds, relationshipFromParent.getName()));
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
    public UpdateBuilder<T> toManyParent(Class<?> parentType, Object parentId,
                                         Property<? extends Collection<T>> relationshipFromParent) {
        return parent(parentType, parentId, relationshipFromParent.getName());
    }

    @Override
    public UpdateBuilder<T> toManyParent(Class<?> parentType, Map<String, Object> parentIds,
                                         Property<? extends Collection<T>> relationshipFromParent) {
        return parent(parentType, parentIds, relationshipFromParent.getName());
    }

    /**
     * @since 2.4
     */
    @Override
    public UpdateBuilder<T> readConstraint(Constraint<T> constraint) {
        context.setReadConstraints(constraint);
        return this;
    }

    /**
     * @since 2.4
     */
    @Override
    public UpdateBuilder<T> writeConstraint(Constraint<T> constraint) {
        context.setWriteConstraints(constraint);
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
    public UpdateBuilder<T> mapper(Property<?> property) {
        return mapper(ByKeyObjectMapperFactory.byKey(property));
    }

    /**
     * @since 1.20
     */
    @Override
    public UpdateBuilder<T> mapper(String propertyName) {
        return mapper(ByKeyObjectMapperFactory.byKey(propertyName));
    }

    /**
     * @since 1.19
     */
    @Override
    @Deprecated
    public UpdateBuilder<T> listener(Object listener) {
        listenersBuilder.addListener(listener);
        return this;
    }

    /**
     * @since 2.7
     */
    @Override
    public <U> UpdateBuilder<T> routingStage(UpdateStage afterStage, Processor<UpdateContext<U>> customStage) {
        return routingStage_NoGenerics(afterStage, customStage);
    }

    private <U> UpdateBuilder<T> routingStage_NoGenerics(UpdateStage afterStage, Processor customStage) {
        processors.compute(afterStage, (s, existing) -> existing != null ? existing.andThen(customStage) : customStage);
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
     * @since 2.13
     */
    @Override
    public UpdateBuilder<T> exclude(List<String> exclude) {
        getOrCreateQueryParams().put(EXCLUDE, exclude);
        return this;
    }

    /**
     * @since 2.13
     */
    @Override
    public UpdateBuilder<T> include(List<String> include) {
        getOrCreateQueryParams().put(INCLUDE, include);
        return this;
    }

    private Map<String, List<String>> getOrCreateQueryParams() {
        if (context.getQueryParams() == null) {
            context.setQueryParams(new HashMap<>());
        }

        return context.getQueryParams();
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
