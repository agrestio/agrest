package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.entity.IExcludeMerger;
import com.nhl.link.rest.runtime.entity.IIncludeMerger;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.13
 */
public class CreateResourceEntityStage implements Processor<UpdateContext<?>> {

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
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {
        LrEntity<T> entity = metadataService.getLrEntity(context.getType());
        ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

        LrRequest request = context.getRawRequest();
        if (request != null) {
            includeMerger.merge(resourceEntity, request.getIncludes());
            excludeMerger.merge(resourceEntity, request.getExcludes());
        }
        context.setEntity(resourceEntity);
    }
}
