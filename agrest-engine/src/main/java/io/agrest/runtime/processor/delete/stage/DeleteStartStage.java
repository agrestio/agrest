package io.agrest.runtime.processor.delete.stage;

import io.agrest.meta.AgSchema;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class DeleteStartStage implements Processor<DeleteContext<?>> {

    private final AgSchema schema;

    public DeleteStartStage(@Inject AgSchema schema) {
        this.schema = schema;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        initAgEntity(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void initAgEntity(DeleteContext<T> context) {
        AgEntityOverlay<T> overlay = context.getEntityOverlay();
        AgEntity<T> entity = schema.getEntity(context.getType());
        context.setAgEntity(entity.resolveOverlay(schema, overlay));
    }
}
