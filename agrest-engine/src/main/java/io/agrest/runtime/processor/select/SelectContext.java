package io.agrest.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.DataResponse;
import io.agrest.HttpStatus;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Maintains state of the request processing chain for select requests.
 *
 * @since 1.16
 */
public class SelectContext<T> extends BaseProcessingContext<T> {

    private final RequestSchema schema;
    private Object unresolvedId;
    private AgObjectId id;
    private EntityParent<?> parent;
    private RootResourceEntity<T> entity;
    private SizeConstraints sizeConstraints;
    private boolean atMostOneObject;
    private Encoder encoder;
    private AgRequestBuilder requestBuilder;
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

    /**
     * Returns a new response object reflecting the context state.
     *
     * @return a new response object reflecting the context state.
     * @since 1.24
     * @deprecated unused anymore, as the context should not be creating a response. It is the responsibility of a
     * pipeline
     */
    @Deprecated(since = "5.0", forRemoval = true)
    public DataResponse<T> createDataResponse() {
        int status = getResponseStatus() != null ? getResponseStatus() : HttpStatus.OK;

        // support null ResourceEntity for cases with terminal stages invoked prior to SelectStage.CREATE_ENTITY
        return entity != null
                ? DataResponse.of(status, entity.getDataWindow()).headers(getResponseHeaders()).total(entity.getData().size()).encoder(encoder).build()
                : DataResponse.of(status, Collections.<T>emptyList()).headers(getResponseHeaders()).build();
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
