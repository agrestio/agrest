package io.agrest.runtime.processor.update;

import io.agrest.AgRequest;
import io.agrest.RootResourceEntity;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IExcludeMerger;
import io.agrest.runtime.entity.IIncludeMerger;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.13
 */
public class CreateResourceEntityStage implements Processor<UpdateContext<?>> {

    private AgDataMap dataMap;
    private IIncludeMerger includeMerger;
    private IExcludeMerger excludeMerger;

    public CreateResourceEntityStage(
            @Inject AgDataMap dataMap,
            @Inject IIncludeMerger includeMerger,
            @Inject IExcludeMerger excludeMerger) {

        this.dataMap = dataMap;
        this.includeMerger = includeMerger;
        this.excludeMerger = excludeMerger;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {
        AgEntity<T> entity = dataMap.getEntity(context.getType());

        // TODO: support entity overlays (second null argument) in updates
        RootResourceEntity<T> resourceEntity = new RootResourceEntity<>(entity, null);

        AgRequest request = context.getMergedRequest();
        if (request != null) {
            // TODO: support entity overlays (second null argument) in updates
            includeMerger.merge(resourceEntity, request.getIncludes(), null);
            excludeMerger.merge(resourceEntity, request.getExcludes());
        }
        context.setEntity(resourceEntity);
    }
}
