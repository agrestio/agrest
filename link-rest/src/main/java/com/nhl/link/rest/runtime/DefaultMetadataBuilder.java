package com.nhl.link.rest.runtime;

import com.nhl.link.rest.MetadataBuilder;
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.MetadataStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.meta.MetadataContext;
import com.nhl.link.rest.runtime.processor.meta.MetadataProcessorFactory;

import javax.ws.rs.core.UriInfo;
import java.util.EnumMap;

/**
 * @since 1.18
 */
public class DefaultMetadataBuilder<T> implements MetadataBuilder<T> {

    // TODO: support custom stages, instead of using empty placeholder for stages
    private static final EnumMap<MetadataStage, Processor<MetadataContext<?>>> PLACEHOLDER
            = new EnumMap<>(MetadataStage.class);


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
    public MetadataResponse<T> process() {
        processorFactory.createProcessor(PLACEHOLDER).execute(context);
        return context.createMetadataResponse();
    }
}
