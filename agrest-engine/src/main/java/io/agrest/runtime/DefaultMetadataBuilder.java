package io.agrest.runtime;

import io.agrest.MetadataBuilder;
import io.agrest.MetadataResponse;
import io.agrest.constraints.Constraint;
import io.agrest.runtime.processor.meta.MetadataContext;
import io.agrest.runtime.processor.meta.MetadataProcessorFactory;

import javax.ws.rs.core.UriInfo;

/**
 * @since 1.18
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public class DefaultMetadataBuilder<T> implements MetadataBuilder<T> {

    protected MetadataContext<T> context;
    private MetadataProcessorFactory processorFactory;

    public DefaultMetadataBuilder(MetadataContext<T> context, MetadataProcessorFactory processorFactory) {
        this.context = context;
        this.processorFactory = processorFactory;
    }

    @Override
    public MetadataBuilder<T> forResource(Class<?> resourceClass) {
        context.setResource(resourceClass);
        return this;
    }

    @Override
    public MetadataBuilder<T> uri(UriInfo uriInfo) {
        context.setUriInfo(uriInfo);
        return this;
    }

    @Override
    public MetadataBuilder<T> constraint(Constraint<T> constraint) {
        context.setConstraint(constraint);
        return this;
    }

    @Override
    public MetadataResponse<T> process() {
        processorFactory.createProcessor().execute(context);
        return context.createMetadataResponse();
    }
}
