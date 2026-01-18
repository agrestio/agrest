package io.agrest.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.RootResourceEntity;
import io.agrest.SizeConstraints;
import io.agrest.access.PathChecker;
import io.agrest.encoder.Encoder;
import io.agrest.id.AgObjectId;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.BaseProcessingContext;
import io.agrest.runtime.EntityParent;
import io.agrest.runtime.meta.RequestSchema;
import org.apache.cayenne.di.Injector;

import java.util.List;
import java.util.Map;

/**
 * Maintains state of the request processing chain for select requests.
 *
 * @since 1.16
 */
public class SelectContext<T> extends BaseProcessingContext<T> {

    private final RequestSchema schema;
    private final AgRequestBuilder requestBuilder;

    private Object unresolvedId;
    private AgObjectId id;
    private EntityParent<?> parent;
    private RootResourceEntity<T> entity;
    private SizeConstraints sizeConstraints;
    private boolean atMostOneObject;
    private Encoder encoder;
    private PathChecker pathChecker;

    public SelectContext(
            Class<T> type,
            RequestSchema schema,
            AgRequestBuilder requestBuilder,
            PathChecker pathChecker,
            Injector injector) {

        super(type, injector);
        this.schema = schema;
        this.requestBuilder = requestBuilder;
        this.pathChecker = pathChecker;
    }

    public boolean isById() {
        return unresolvedId != null;
    }

    /**
     * @since 5.0
     */
    public Object getUnresolvedId() {
        return unresolvedId;
    }

    /**
     * @since 5.0
     */
    public void setUnresolvedId(Object unresolvedId) {
        this.unresolvedId = unresolvedId;
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
    public <A> void addEntityOverlay(AgEntityOverlay<A> overlay) {
        schema.addOverlay(overlay);
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
