package io.agrest.runtime.processor.update;

import io.agrest.AgRequest;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IExcludeMerger;
import io.agrest.runtime.entity.IIncludeMerger;
import io.agrest.runtime.meta.IMetadataService;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.13
 */
public class CreateResourceEntityStage implements Processor<UpdateContext<?, ?>> {

    private IMetadataService metadataService;
    private IIncludeMerger includeMerger;
    private IExcludeMerger excludeMerger;

    public CreateResourceEntityStage(
            @Inject IMetadataService metadataService,
            @Inject IIncludeMerger includeMerger,
            @Inject IExcludeMerger excludeMerger) {

        this.metadataService = metadataService;
        this.includeMerger = includeMerger;
        this.excludeMerger = excludeMerger;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?, ?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T, E> void doExecute(UpdateContext<T, E> context) {
        AgEntity<T> entity = metadataService.getAgEntity(context.getType());
        ResourceEntity<T, E> resourceEntity = new ResourceEntity<>(entity);

        AgRequest request = context.getRawRequest();
        if (request != null) {
            includeMerger.merge(resourceEntity, request.getIncludes());
            excludeMerger.merge(resourceEntity, request.getExcludes());
        }
        context.setEntity(resourceEntity);
    }
}
