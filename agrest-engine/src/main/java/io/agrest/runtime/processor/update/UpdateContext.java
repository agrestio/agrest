package io.agrest.runtime.processor.update;

import io.agrest.AgException;
import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapperFactory;
import io.agrest.RootResourceEntity;
import io.agrest.access.PathChecker;
import io.agrest.encoder.Encoder;
import io.agrest.id.AgObjectId;
import io.agrest.processor.BaseProcessingContext;
import io.agrest.runtime.EntityParent;
import io.agrest.runtime.meta.RequestSchema;
import org.apache.cayenne.di.Injector;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains state of the request processing chain for various updating
 * requests.
 *
 * @since 1.16
 */
public class UpdateContext<T> extends BaseProcessingContext<T> {

    private final RequestSchema schema;

    private RootResourceEntity<T> entity;
    private AgObjectId id;
    private EntityParent<?> parent;
    private boolean includingDataInResponse;
    private ObjectMapperFactory mapper;
    private String entityData;

    private Collection<EntityUpdate<T>> updates;
    private Encoder encoder;
    private PathChecker pathChecker;

    private final AgRequestBuilder requestBuilder;
    private final Map<ChangeOperationType, List<ChangeOperation<T>>> changeOperations;

    @Deprecated
    private boolean idUpdatesDisallowed;

    public UpdateContext(
            Class<T> type,
            RequestSchema schema,
            AgRequestBuilder requestBuilder,
            PathChecker pathChecker,
            Injector injector) {

        super(type, injector);

        this.schema = schema;

        this.changeOperations = new EnumMap<>(ChangeOperationType.class);
        this.changeOperations.put(ChangeOperationType.CREATE, Collections.emptyList());
        this.changeOperations.put(ChangeOperationType.UPDATE, Collections.emptyList());
        this.changeOperations.put(ChangeOperationType.DELETE, Collections.emptyList());

        this.requestBuilder = requestBuilder;
        this.pathChecker = pathChecker;
    }

    /**
     * Returns a newly created DataResponse object reflecting the context state.
     *
     * @return a newly created DataResponse object reflecting the context state.
     * @since 1.24
     */
    public DataResponse<T> createDataResponse() {
        // support null ResourceEntity for cases with custom terminal stages
        return entity != null
                ? DataResponse.of(entity.getDataWindow()).status(getStatus()).total(entity.getData().size()).encoder(encoder).build()
                : DataResponse.of(Collections.<T>emptyList()).status(getStatus()).build();
    }

    /**
     * @since 1.19
     */
    public Collection<EntityUpdate<T>> getUpdates() {
        return updates;
    }

    public void setUpdates(Collection<EntityUpdate<T>> updates) {
        this.updates = updates;
    }

    /**
     * Returns first update object. Throws unless this response contains exactly
     * one update.
     *
     * @since 1.19
     */
    public EntityUpdate<T> getFirst() {

        Collection<EntityUpdate<T>> updates = getUpdates();

        if (updates.size() != 1) {
            throw AgException.internalServerError("Expected one object in update. Actual: %s", updates.size());
        }

        return updates.iterator().next();
    }

    public boolean isById() {
        return id != null;
    }

    public AgObjectId getId() {
        return id;
    }

    public void setId(AgObjectId id) {
        this.id = id;
    }

    public EntityParent<?> getParent() {
        return parent;
    }

    public void setParent(EntityParent<?> parent) {
        this.parent = parent;
    }

    public boolean isIncludingDataInResponse() {
        return includingDataInResponse;
    }

    public void setIncludingDataInResponse(boolean includeData) {
        this.includingDataInResponse = includeData;
    }

    public ObjectMapperFactory getMapper() {
        return mapper;
    }

    public void setMapper(ObjectMapperFactory mapper) {
        this.mapper = mapper;
    }


    public String getEntityData() {
        return entityData;
    }

    public void setEntityData(String entityData) {
        this.entityData = entityData;
    }

    /**
     * @since 1.19
     * @deprecated not initialized and unused since 5.0, as ID permissions checking happens elsewhere, and doesn't need
     * to be in the context
     */
    @Deprecated(since = "5.0")
    public boolean isIdUpdatesDisallowed() {
        return idUpdatesDisallowed;
    }

    /**
     * @since 1.19
     * @deprecated not initialized and unused since 5.0, as ID permissions checking happens elsewhere, and doesn't need
     * to be in the context
     */
    @Deprecated(since = "5.0")
    public void setIdUpdatesDisallowed(boolean idUpdatesDisallowed) {
        this.idUpdatesDisallowed = idUpdatesDisallowed;
    }

    /**
     * @since 1.20
     */
    public RootResourceEntity<T> getEntity() {
        return entity;
    }

    /**
     * @since 1.20
     */
    public void setEntity(RootResourceEntity<T> entity) {
        this.entity = entity;
    }

    /**
     * @since 1.24
     */
    public Encoder getEncoder() {
        return encoder;
    }

    /**
     * @since 1.24
     */
    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

    /**
     * @since 4.8
     */
    public Map<ChangeOperationType, List<ChangeOperation<T>>> getChangeOperations() {
        return changeOperations;
    }

    /**
     * @since 4.8
     */
    public void setChangeOperations(ChangeOperationType type, List<ChangeOperation<T>> ops) {
        this.changeOperations.put(type, ops);
    }

    /**
     * Returns an object encapsulating Ag protocol parameters of the current request.
     *
     * @since 2.13
     */
    public AgRequest getRequest() {
        return requestBuilder.build();
    }

    /**
     * Merges provided request object with any previously stored protocol parameters.
     *
     * @since 5.0
     */
    public void setRequest(AgRequest request) {
        requestBuilder.setRequest(request);
    }

    /**
     * @since 5.0
     */
    public PathChecker getMaxPathDepth() {
        return pathChecker;
    }

    /**
     * @since 5.0
     */
    public void setMaxPathDepth(PathChecker pathChecker) {
        this.pathChecker = pathChecker;
    }

    /**
     * Merges provided request object with any previously stored protocol parameters.
     *
     * @since 5.0
     */
    public void mergeClientParameters(Map<String, List<String>> params) {
        requestBuilder.mergeClientParams(params);
    }

    /**
     * @since 5.0
     */
    public RequestSchema getSchema() {
        return schema;
    }
}
