package io.agrest.runtime.processor.update;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.AgRequest;
import io.agrest.CompoundObjectId;
import io.agrest.DataResponse;
import io.agrest.EntityParent;
import io.agrest.EntityUpdate;
import io.agrest.ObjectMapperFactory;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.encoder.Encoder;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.BaseProcessingContext;

import javax.ws.rs.core.UriInfo;
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
    private UriInfo uriInfo;
    private AgObjectId id;
    private EntityParent<?> parent;
    private boolean includingDataInResponse;
    private ObjectMapperFactory mapper;
    private String entityData;
    private boolean idUpdatesDisallowed;
    private Collection<EntityUpdate<T>> updates;
    private Encoder encoder;
    private AgRequest mergedRequest;
    private AgRequest request;
    private Map<Class<?>, AgEntityOverlay<?>> entityOverlays;
    private final Map<ChangeOperationType, List<ChangeOperation<T>>> changeOperations;

    public UpdateContext(Class<T> type) {
        super(type);
        this.changeOperations = new EnumMap<>(ChangeOperationType.class);
        changeOperations.put(ChangeOperationType.CREATE, Collections.emptyList());
        changeOperations.put(ChangeOperationType.UPDATE, Collections.emptyList());
        changeOperations.put(ChangeOperationType.DELETE, Collections.emptyList());
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
                ? DataResponse.of(getStatus(), entity.getDataWindow(), entity.getResult().size(), encoder)
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

    /**
     * @since 2.13
     */
    public Map<String, List<String>> getProtocolParameters() {
        return uriInfo != null ? uriInfo.getQueryParameters() : Collections.emptyMap();
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public boolean isById() {
        return id != null;
    }

    public AgObjectId getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = new SimpleObjectId(id);
    }

    public void setCompoundId(Map<String, Object> ids) {
        this.id = new CompoundObjectId(ids);
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
     * Returns AgRequest instance that is the source of request data for {@link io.agrest.UpdateStage#CREATE_ENTITY}
     * stage that produces a tree of {@link ResourceEntity} instances. Usually merged request is a result of merging
     * context AgRequest with URL parameters during {@link io.agrest.UpdateStage#PARSE_REQUEST} stage.
     *
     * @since 3.2
     */
    public AgRequest getMergedRequest() {
        return mergedRequest;
    }

    /**
     * Sets AgRequest instance that is the source of request data for {@link io.agrest.UpdateStage#CREATE_ENTITY} stage
     * to create a tree of {@link ResourceEntity} instances.
     *
     * @since 3.2
     */
    public void setMergedRequest(AgRequest request) {
        this.mergedRequest = request;
    }

    /**
     * Returns a request object, previously explicitly passed to the select chain in the endpoint method. Depending on
     * the calling chain configuration, this object is either used directly to serve the request, or is combined with
     * URL parameters during {@link io.agrest.UpdateStage#PARSE_REQUEST}, producing a "mergedRequest".
     *
     * @since 2.13
     */
    public AgRequest getRequest() {
        return request;
    }

    /**
     * Sets a request object. Depending on the calling chain configuration, this object is either used directly to
     * serve the request, or is combined with URL parameters during {@link io.agrest.SelectStage#PARSE_REQUEST},
     * producing a "mergedRequest".
     *
     * @since 2.13
     */
    public void setRequest(AgRequest request) {
        this.request = request;
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
}
