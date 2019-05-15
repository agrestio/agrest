package io.agrest.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.CompoundObjectId;
import io.agrest.DataResponse;
import io.agrest.EntityParent;
import io.agrest.EntityProperty;
import io.agrest.AgObjectId;
import io.agrest.ResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.SizeConstraints;
import io.agrest.constraints.Constraint;
import io.agrest.encoder.Encoder;
import io.agrest.processor.BaseProcessingContext;

import javax.ws.rs.core.UriInfo;
import java.util.Collections;
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
    private ResourceEntity<T> entity;
    private UriInfo uriInfo;
    private Map<String, EntityProperty> extraProperties;
    private SizeConstraints sizeConstraints;
    private Constraint<T> constraint;
    private boolean atMostOneObject;
    private Encoder encoder;
    private AgRequest mergedRequest;
    private AgRequest request;


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
        List<? extends T> objects = this.entity != null ? this.entity.getResult() : Collections.<T>emptyList();
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

    public Map<String, EntityProperty> getExtraProperties() {
        return extraProperties;
    }

    public void setExtraProperties(Map<String, EntityProperty> extraProperties) {
        this.extraProperties = extraProperties;
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
     */
    public Constraint<T> getConstraint() {
        return constraint;
    }

    /**
     * @param constraint constraint function.
     * @since 2.4
     */
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

    /**
     * @since 1.20
     */
    public ResourceEntity<T> getEntity() {
        return entity;
    }

    /**
     * @since 1.20
     */
    public void setEntity(ResourceEntity<T> entity) {
        this.entity = entity;
    }

    /**
     * Returns AgRequest instance that is a result of merging context AgRequest with URL parameters. Used in
     * {@link CreateResourceEntityStage} to create a tree of {@link ResourceEntity} instances for the request.
     *
     * @since 3.2
     */
    public AgRequest getMergedRequest() {
        return mergedRequest;
    }

    /**
     * Saves AgRequest that contains query parameters.
     * <p>
     * This AgRequest object is build from two sources.
     * 1. Parse UriInfo and create query parameters objects.
     * 2. If some of query parameters are passed explicitly they will be used instead of parsing from UriInfo.
     * These explicit query parameters are saved in mergedRequest object during ParseRequestStage.
     *
     * @since 2.13
     */
    public void setMergedRequest(AgRequest request) {
        this.mergedRequest = request;
    }

    /**
     * Returns AgRequest object that contains query parameters explicitly passed through API method call
     *
     * @since 2.13
     */
    public AgRequest getRequest() {
        return request;
    }

    /**
     * Saves AgRequest object that contains query parameters explicitly passed through API method call
     * These parameters are created during ConvertQueryParamsStage
     *
     * <pre>{@code
     *
     * 		public DataResponse<E2> getE2(@Context UriInfo uriInfo, @QueryParam CayenneExp cayenneExp) {
     * 			// Explicit query parameter
     * 			AgRequest agRequest = AgRequest.builder().cayenneExp(cayenneExp).build();
     *
     * 			return Ag.service(config).select(E2.class)
     * 							.uri(uriInfo)
     * 							.request(agRequest) // overrides parameters from uriInfo
     * 							.get();
     *        }
     *
     * }</pre>
     *
     * @since 2.13
     */
    public void setRequest(AgRequest request) {
        this.request = request;
    }

    /**
     * @since 2.13
     * @deprecated since 3.2 in favor of {@link #getMergedRequest()}.
     */
    @Deprecated
    public AgRequest getRawRequest() {
        return getMergedRequest();
    }

    /**
     * @since 2.13
     * @deprecated since 3.2 in favor of {@link #setMergedRequest(AgRequest)}
     */
    @Deprecated
    public void setRawRequest(AgRequest request) {
        this.mergedRequest = request;
    }
}
