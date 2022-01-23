package io.agrest.runtime.processor.update.stage;

import io.agrest.UpdateStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * A processor invoked for {@link io.agrest.UpdateStage#MERGE_CHANGES} stage.
 *
 * @since 5.0
 */
public class UpdateMergeChangesStage implements Processor<UpdateContext<?>> {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        throw new UnsupportedOperationException(
                "No implementation of " + UpdateStage.MERGE_CHANGES + " stage is available");
    }
}
