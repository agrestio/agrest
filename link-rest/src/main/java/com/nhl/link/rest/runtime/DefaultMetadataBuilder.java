package com.nhl.link.rest.runtime;

import com.nhl.link.rest.MetadataBuilder;
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.runtime.processor.meta.MetadataContext;
import com.nhl.link.rest.runtime.processor.meta.MetadataProcessorFactory;

import javax.ws.rs.core.UriInfo;

/**
 * @since 1.18
 */
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
