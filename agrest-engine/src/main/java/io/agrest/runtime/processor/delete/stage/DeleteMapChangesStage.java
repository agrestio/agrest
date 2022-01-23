package io.agrest.runtime.processor.delete.stage;

import io.agrest.DeleteStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;

/**
 * @since 5.0
 */
public class DeleteMapChangesStage implements Processor<DeleteContext<?>> {

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        throw new UnsupportedOperationException(
                "No implementation of " + DeleteStage.MAP_CHANGES + " stage is available");
    }
}
