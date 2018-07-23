package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.tree.IExcludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeConstructor;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.13
 */
public class ConstructResourceEntityStage implements Processor<UpdateContext<?>> {

    private IMetadataService metadataService;
    private IIncludeConstructor includeConstructor;
    private IExcludeConstructor excludeConstructor;

    public ConstructResourceEntityStage(
            @Inject IMetadataService metadataService,
            @Inject IIncludeConstructor includeConstructor,
            @Inject IExcludeConstructor excludeConstructor) {

        this.metadataService = metadataService;
        this.includeConstructor = includeConstructor;
        this.excludeConstructor = excludeConstructor;
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
            includeConstructor.construct(resourceEntity, request.getIncludes());
            excludeConstructor.construct(resourceEntity, request.getExcludes());
        }
        context.setEntity(resourceEntity);
    }
}
