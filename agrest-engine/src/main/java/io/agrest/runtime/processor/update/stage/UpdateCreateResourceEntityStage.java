package io.agrest.runtime.processor.update.stage;

import io.agrest.AgRequest;
import io.agrest.RootResourceEntity;
import io.agrest.meta.AgSchema;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
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

    private final AgSchema schema;
    private final IIncludeMerger includeMerger;
    private final IExcludeMerger excludeMerger;

    public UpdateCreateResourceEntityStage(
            @Inject AgSchema schema,
            @Inject IIncludeMerger includeMerger,
            @Inject IExcludeMerger excludeMerger) {

        this.schema = schema;
        this.includeMerger = includeMerger;
        this.excludeMerger = excludeMerger;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {
        AgEntityOverlay<T> overlay = context.getEntityOverlay(context.getType());
        AgEntity<T> entity = schema.getEntity(context.getType());

        RootResourceEntity<T> resourceEntity = new RootResourceEntity<>(
                overlay != null ? overlay.resolve(schema, entity) : entity
        );

        AgRequest request = context.getRequest();
        includeMerger.merge(resourceEntity, request.getIncludes(), context.getEntityOverlays());
        excludeMerger.merge(resourceEntity, request.getExcludes());

        context.setEntity(resourceEntity);
    }
}
