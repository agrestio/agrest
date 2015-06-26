package com.nhl.link.rest.runtime;

import com.nhl.link.rest.MetadataBuilder;
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.meta.MetadataContext;

import javax.ws.rs.core.UriInfo;

/**
 * @since 1.18
 */
public class DefaultMetadataBuilder<T> implements MetadataBuilder<T> {

    protected MetadataContext<T> context;
    private Processor<MetadataContext<T>, T> processor;

    public DefaultMetadataBuilder(MetadataContext<T> context, Processor<MetadataContext<T>, T> processor) {
        this.context = context;
        this.processor = processor;
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
    public MetadataResponse<T> process() {
        processor.execute(context);
        return context.getResponse();
    }

}
