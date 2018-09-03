package io.agrest.runtime.processor.meta;

import io.agrest.MetadataResponse;
import io.agrest.constraints.Constraint;
import io.agrest.encoder.Encoder;
import io.agrest.meta.LrResource;
import io.agrest.processor.BaseProcessingContext;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;

/**
 * @since 1.18
 */
public class MetadataContext<T> extends BaseProcessingContext<T> {

    private Class<?> resourceType;
    private UriInfo uriInfo;
    private Encoder encoder;
    private Constraint<T> constraint;
    private Collection<LrResource<T>> resources;

    public MetadataContext(Class<T> type) {
        super(type);
    }

    /**
     * Returns a new response object reflecting the context state.
     *
     * @return a newly created response object reflecting the context state.
     * @since 1.24
     */
    public MetadataResponse<T> createMetadataResponse() {
        MetadataResponse<T> response = new MetadataResponse<>(getType());
        response.setEncoder(encoder);
        response.setResources(resources);

        return response;
    }

    public void setResource(Class<?> resourceClass) {
        this.resourceType = resourceClass;
    }

    public Class<?> getResource() {
        return resourceType;
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
     * @since 1.24
     */
    public Collection<LrResource<T>> getResources() {
        return resources;
    }

    /**
     * @since 1.24
     */
    public void setResources(Collection<LrResource<T>> resources) {
        this.resources = resources;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    /**
     * @return application base URL calculated from the current request.
     * @deprecated since 2.10 unused. Base URL for metadata is calculated differently.
     */
    @Deprecated
    public String getApplicationBase() {
        if (uriInfo == null) {
            return null;
        }
        return uriInfo.getBaseUri().toString();
    }

    /**
     * @return this context's constraint function.
     * @since 2.10
     */
    public Constraint<T> getConstraint() {
        return constraint;
    }

    /**
     * @param constraint constraint function.
     * @since 2.10
     */
    public void setConstraint(Constraint<T> constraint) {
        this.constraint = constraint;
    }
}
