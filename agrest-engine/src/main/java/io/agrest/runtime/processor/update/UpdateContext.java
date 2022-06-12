package io.agrest.runtime.processor.update;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.DataResponse;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapperFactory;
import io.agrest.RootResourceEntity;
import io.agrest.encoder.Encoder;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.BaseProcessingContext;
import org.apache.cayenne.di.Injector;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains state of the request processing chain for various updating
 * requests.
 *
 * @since 1.16
 */
public class UpdateContext<T> extends BaseProcessingContext<T> {

    private RootResourceEntity<T> entity;
    private AgObjectId id;
    private EntityParent<?> parent;
    private boolean includingDataInResponse;
    private ObjectMapperFactory mapper;
    private String entityData;
    private boolean idUpdatesDisallowed;
    private Collection<EntityUpdate<T>> updates;
    private Encoder encoder;
    private Map<Class<?>, AgEntityOverlay<?>> entityOverlays;

    private final AgRequestBuilder requestBuilder;
    private final Map<ChangeOperationType, List<ChangeOperation<T>>> changeOperations;

    public UpdateContext(Class<T> type, AgRequestBuilder requestBuilder, Injector injector) {
        super(type, injector);

        this.changeOperations = new EnumMap<>(ChangeOperationType.class);
        this.changeOperations.put(ChangeOperationType.CREATE, Collections.emptyList());
        this.changeOperations.put(ChangeOperationType.UPDATE, Collections.emptyList());
        this.changeOperations.put(ChangeOperationType.DELETE, Collections.emptyList());

        this.requestBuilder = requestBuilder;
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
                ? DataResponse.of(getStatus(), entity.getDataWindow(), entity.getData().size(), encoder)
                : DataResponse.of(getStatus());
    }

    /**
     * @since 1.19
     */
    public boolean hasChanges() {

        for (EntityUpdate<T> u : updates) {
            if (u.hasChanges()) {
                return true;
            }
        }

        return false;
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

    /**
     * @since 4.8
     */
    public Map<Class<?>, AgEntityOverlay<?>> getEntityOverlays() {
        return entityOverlays != null ? entityOverlays : Collections.emptyMap();
    }

    /**
     * @since 4.8
     */
    public <A> AgEntityOverlay<A> getEntityOverlay(Class<A> type) {
        return entityOverlays != null ? (AgEntityOverlay<A>) entityOverlays.get(type) : null;
    }

    /**
     * @since 4.8
     */
    public <A> void addEntityOverlay(AgEntityOverlay<A> overlay) {
        getOrCreateOverlay(overlay.getType()).merge(overlay);
    }

    private <A> AgEntityOverlay<A> getOrCreateOverlay(Class<A> type) {
        if (entityOverlays == null) {
            entityOverlays = new HashMap<>();
        }

        return (AgEntityOverlay<A>) entityOverlays.computeIfAbsent(type, AgEntityOverlay::new);
    }

    public String getEntityData() {
        return entityData;
    }

    public void setEntityData(String entityData) {
        this.entityData = entityData;
    }

    /**
     * @since 1.19
     */
    public boolean isIdUpdatesDisallowed() {
        return idUpdatesDisallowed;
    }

    /**
     * @since 1.19
     */
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
     * Merges provided request object with any previously stored protocol parameters.
     *
     * @since 5.0
     */
    public void mergeClientParameters(Map<String, List<String>> params) {
        requestBuilder.mergeClientParams(params);
    }
}
