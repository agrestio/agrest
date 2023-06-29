package io.agrest.runtime.processor.delete.stage;

import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;

/**
 * @since 5.0
 */
public class DeleteStartStage implements Processor<DeleteContext<?>> {

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        initAgEntity(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void initAgEntity(DeleteContext<T> context) {
        AgEntity<T> entity = context.getSchema().getEntity(context.getType());
        context.setAgEntity(entity);
    }
}
