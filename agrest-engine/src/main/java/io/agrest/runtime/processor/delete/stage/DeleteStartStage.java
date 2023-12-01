package io.agrest.runtime.processor.delete.stage;

import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IIdResolver;
import io.agrest.runtime.processor.delete.DeleteContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class DeleteStartStage implements Processor<DeleteContext<?>> {

    private final IIdResolver idResolver;

    public DeleteStartStage(@Inject IIdResolver idResolver) {
        this.idResolver = idResolver;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        initAgEntity(context);
        resolveIds(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void initAgEntity(DeleteContext<T> context) {
        AgEntity<T> entity = context.getSchema().getEntity(context.getType());
        context.setAgEntity(entity);
    }

    protected <T> void resolveIds(DeleteContext<T> context) {
        context.setIds(idResolver.resolve(context.getAgEntity(), context.getUnresolvedIds()));
    }
}
