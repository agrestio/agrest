package io.agrest.cayenne.processor.update.stage;

import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateMapChangesStage;
import org.apache.cayenne.Persistent;

/**
 * A superclass of processors for the {@link io.agrest.UpdateStage#MAP_CHANGES} stage that associates persistent
 * objects with update operations.
 *
 * @since 4.8
 */
public abstract class CayenneMapChangesStage extends UpdateMapChangesStage {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        UpdateContext<Persistent> doContext = (UpdateContext<Persistent>) context;
        map(doContext);
        return ProcessorOutcome.CONTINUE;
    }

    protected abstract <T extends Persistent> void map(UpdateContext<T> context);
}
