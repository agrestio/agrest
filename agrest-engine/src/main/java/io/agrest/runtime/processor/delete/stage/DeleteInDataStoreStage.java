package io.agrest.runtime.processor.delete.stage;

import io.agrest.DeleteStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;

/**
 * @since 5.0
 */
public class DeleteInDataStoreStage implements Processor<DeleteContext<?>> {

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        throw new UnsupportedOperationException(
                "No implementation of " + DeleteStage.DELETE_IN_DATA_STORE + " stage is available");
    }
}
