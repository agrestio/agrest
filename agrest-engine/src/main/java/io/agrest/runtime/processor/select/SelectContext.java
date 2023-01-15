package io.agrest.runtime.processor.select;

import io.agrest.access.MaxPathDepth;
import io.agrest.id.AgObjectId;
import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.DataResponse;
import io.agrest.runtime.EntityParent;
import io.agrest.RootResourceEntity;
import io.agrest.SizeConstraints;
import io.agrest.encoder.Encoder;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.BaseProcessingContext;
import org.apache.cayenne.di.Injector;

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
    private SizeConstraints sizeConstraints;
    private boolean atMostOneObject;
    private Encoder encoder;
    private AgRequestBuilder requestBuilder;
    private Map<Class<?>, AgEntityOverlay<?>> entityOverlays;
    private MaxPathDepth maxPathDepth;

    public SelectContext(Class<T> type, AgRequestBuilder requestBuilder, MaxPathDepth maxPathDepth, Injector injector) {
        super(type, injector);
        this.requestBuilder = requestBuilder;
        this.maxPathDepth = maxPathDepth;
    }

    /**
     * Returns a new response object reflecting the context state.
     *
     * @return a new response object reflecting the context state.
     * @since 1.24
     */
    public DataResponse<T> createDataResponse() {
        // support null ResourceEntity for cases with custom terminal stages
        return entity != null
                ? DataResponse.of(entity.getDataWindow()).status(getStatus()).total(entity.getData().size()).encoder(encoder).build()
                : DataResponse.of(Collections.<T>emptyList()).status(getStatus()).build();
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
     * Returns an object encapsulating Ag protocol parameters of the current request.
     *
     * @since 2.13
     */
    public AgRequest getRequest() {
        return requestBuilder.build();
    }

    /**
     * Overrides all collected protocol values with parameters from the provided request.
     *
     * @since 5.0
     */
    public void setRequest(AgRequest request) {
        requestBuilder.setRequest(request);
    }

    /**
     * @since 5.0
     */
    public MaxPathDepth getMaxPathDepth() {
        return maxPathDepth;
    }

    /**
     * @since 5.0
     */
    public void setMaxPathDepth(MaxPathDepth maxPathDepth) {
        this.maxPathDepth = maxPathDepth;
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
