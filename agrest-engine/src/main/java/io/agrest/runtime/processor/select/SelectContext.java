package io.agrest.runtime.processor.select;

import io.agrest.*;
import io.agrest.constraints.Constraint;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.BaseProcessingContext;

import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains state of the request processing chain for select requests.
 *
 * @since 1.16
 */
public class SelectContext<T> extends BaseProcessingContext<T> {

    private AgObjectId id;
    private EntityParent<?> parent;
    private RootResourceEntity<T> entity;
    private UriInfo uriInfo;
    private SizeConstraints sizeConstraints;
    private Constraint<T> constraint;
    private boolean atMostOneObject;
    private Encoder encoder;
    private AgRequest mergedRequest;
    private AgRequest request;
    private List<EntityEncoderFilter> entityEncoderFilters;
    private Map<Class<?>, AgEntityOverlay<?>> entityOverlays;

    public SelectContext(Class<T> type) {
        super(type);
    }

    /**
     * Returns a new response object reflecting the context state.
     *
     * @return a new response object reflecting the context state.
     * @since 1.24
     */
    public DataResponse<T> createDataResponse() {
        List<? extends T> objects = this.entity != null ? this.entity.getResult() : Collections.emptyList();
        DataResponse<T> response = DataResponse.forType(getType());
        response.setObjects(objects);
        response.setEncoder(encoder);
        response.setStatus(getStatus());
        return response;
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

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    /**
     * @since 2.5
     */
    public Map<String, List<String>> getProtocolParameters() {
        return uriInfo != null ? uriInfo.getQueryParameters() : Collections.emptyMap();
    }

    /**
     * @since 3.4
     */
    public List<EntityEncoderFilter> getEntityEncoderFilters() {
        return entityEncoderFilters;
    }

    /**
     * @since 3.4
     */
    public void setEntityEncoderFilters(List<EntityEncoderFilter> entityEncoderFilters) {
        this.entityEncoderFilters = entityEncoderFilters;
    }

    /**
     * @since 3.4
     */
    public Map<Class<?>, AgEntityOverlay<?>> getEntityOverlays() {
        return entityOverlays != null ? entityOverlays : Collections.emptyMap();
    }

    /**
     * @since 3.4
     */
    public <A> AgEntityOverlay<A> getEntityOverlay(Class<A> type) {
        return entityOverlays != null ? (AgEntityOverlay<A>) entityOverlays.get(type) : null;
    }

    /**
     * @since 3.4
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

    public SizeConstraints getSizeConstraints() {
        return sizeConstraints;
    }

    public void setSizeConstraints(SizeConstraints sizeConstraints) {
        this.sizeConstraints = sizeConstraints;
    }


    /**
     * @return this context's constraint function.
     * @since 2.4
     * @deprecated since 4.8 as Constraint class is deprecated
     */
    @Deprecated
    public Constraint<T> getConstraint() {
        return constraint;
    }

    /**
     * @param constraint constraint function.
     * @since 2.4
     * @deprecated since 4.8 as Constraint class is deprecated
     */
    @Deprecated
    public void setConstraint(Constraint<T> constraint) {
        this.constraint = constraint;
    }

    public boolean isAtMostOneObject() {
        return atMostOneObject;
    }

    public void setAtMostOneObject(boolean expectingOne) {
        this.atMostOneObject = expectingOne;
    }

    public Encoder getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder encoder) {
        this.encoder = encoder;
    }

    public RootResourceEntity<T> getEntity() {
        return entity;
    }

    public void setEntity(RootResourceEntity<T> entity) {
        this.entity = entity;
    }

    /**
     * Returns AgRequest instance that is the source of request data for {@link io.agrest.SelectStage#CREATE_ENTITY}
     * stage that produces a tree of {@link ResourceEntity} instances. Usually merged request is a result of merging
     * context AgRequest with URL parameters during {@link io.agrest.SelectStage#PARSE_REQUEST} stage.
     *
     * @since 3.2
     */
    public AgRequest getMergedRequest() {
        return mergedRequest;
    }

    /**
     * Sets AgRequest instance that is the source of request data for {@link io.agrest.SelectStage#CREATE_ENTITY} stage
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
     * URL parameters during {@link io.agrest.SelectStage#PARSE_REQUEST}, producing a "mergedRequest".
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
}
