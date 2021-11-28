package io.agrest.cayenne.processor.update;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.UpdateOperation;
import org.apache.cayenne.DataObject;

import java.util.List;

/**
 * A superclass of processors for the {@link io.agrest.UpdateStage#MAP_CHANGES} stage that associates persistent
 * objects with update operations.
 *
 * @since 4.8
 */
public abstract class CayenneMapChangesStage implements Processor<UpdateContext<?>> {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        UpdateContext<DataObject> doContext = (UpdateContext<DataObject>) context;
        doContext.setUpdateOperations(map(doContext));
        return ProcessorOutcome.CONTINUE;
    }

    protected abstract <T extends DataObject> List<UpdateOperation<T>> map(UpdateContext<T> context);
}
