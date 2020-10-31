package io.agrest.runtime.processor.meta;

import io.agrest.MetadataResponse;
import io.agrest.constraints.Constraint;
import io.agrest.encoder.Encoder;
import io.agrest.meta.AgResource;
import io.agrest.processor.BaseProcessingContext;

import javax.ws.rs.core.UriInfo;
import java.util.Collection;

/**
 * @since 1.18
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public class MetadataContext<T> extends BaseProcessingContext<T> {

    private Class<?> resourceType;
    private UriInfo uriInfo;
    private Encoder encoder;
    private Constraint<T> constraint;
    private Collection<AgResource<T>> resources;

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
    public Collection<AgResource<T>> getResources() {
        return resources;
    }

    /**
     * @since 1.24
     */
    public void setResources(Collection<AgResource<T>> resources) {
        this.resources = resources;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
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
