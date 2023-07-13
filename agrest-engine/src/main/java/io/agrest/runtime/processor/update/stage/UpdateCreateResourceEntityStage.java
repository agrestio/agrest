package io.agrest.runtime.processor.update.stage;

import io.agrest.AgRequest;
import io.agrest.RootResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IExcludeMerger;
import io.agrest.runtime.entity.IIncludeMerger;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class UpdateCreateResourceEntityStage implements Processor<UpdateContext<?>> {

    private final IIncludeMerger includeMerger;
    private final IExcludeMerger excludeMerger;

    public UpdateCreateResourceEntityStage(
            @Inject IIncludeMerger includeMerger,
            @Inject IExcludeMerger excludeMerger) {

        this.includeMerger = includeMerger;
        this.excludeMerger = excludeMerger;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {
        AgEntity<T> entity = context.getSchema().getEntity(context.getType());
        RootResourceEntity<T> resourceEntity = new RootResourceEntity<>(entity);

        if (context.isIncludingDataInResponse()) {
            AgRequest request = context.getRequest();
            includeMerger.merge(resourceEntity,
                    request.getIncludes(),
                    context.getSchema(),
                    context.getMaxPathDepth());

            excludeMerger.merge(resourceEntity, request.getExcludes());
        }

        context.setEntity(resourceEntity);
    }
}
