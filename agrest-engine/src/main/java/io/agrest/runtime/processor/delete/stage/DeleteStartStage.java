package io.agrest.runtime.processor.delete.stage;

import io.agrest.meta.AgDataMap;
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

    private final AgDataMap dataMap;

    public DeleteStartStage(@Inject AgDataMap dataMap) {
        this.dataMap = dataMap;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        initAgEntity(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void initAgEntity(DeleteContext<T> context) {
        AgEntityOverlay<T> overlay = context.getEntityOverlay();
        AgEntity<T> entity = dataMap.getEntity(context.getType());
        context.setAgEntity(overlay != null ? overlay.resolve(dataMap, entity) : entity);
    }
}
