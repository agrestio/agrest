package io.agrest.cayenne.processor.update.stage;

import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateMapChangesStage;
import org.apache.cayenne.DataObject;

/**
 * A superclass of processors for the {@link io.agrest.UpdateStage#MAP_CHANGES} stage that associates persistent
 * objects with update operations.
 *
 * @since 4.8
 */
public abstract class CayenneMapChangesStage extends UpdateMapChangesStage {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        UpdateContext<DataObject> doContext = (UpdateContext<DataObject>) context;
        map(doContext);
        return ProcessorOutcome.CONTINUE;
    }

    protected abstract <T extends DataObject> void map(UpdateContext<T> context);
}
